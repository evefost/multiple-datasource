
package com.eve.multiple.config;


import com.eve.multiple.*;
import com.eve.multiple.datasource.MultipleDataSource;
import com.eve.multiple.interceptor.PreTransactionInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xieyang
 */

public class DatasourceConfig implements SmartInitializingSingleton, BeanDefinitionRegistryPostProcessor,EnvironmentAware {


    public final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String TRANSACTION_MANAGER_PREFIX = "transactionManager_";

    private static final String SHARE_DEFAULT="datasource.share.default";

    private static final String TENANT_ENABLE="datasource.tenant.enable";


    private BeanDefinitionRegistry registry;

    private MultipleSourceProperties<DataSourceProperties> sourceProperties = new MultipleSourceProperties();

    private MultipleDataSource dataSource;

    private DataSourceResolver dataSourceResolver;

    private DatasourcePropertiesLoader propertiesLoader;

    private Environment environment;

    public void setDataSourceResolver(DataSourceResolver dataSourceResolver) {
        this.dataSourceResolver = dataSourceResolver;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        registryBeanDefinitions(beanFactory);

        //初始化数据源连接池
        initDatasource();
    }


    private void registryBeanDefinitions(ConfigurableListableBeanFactory beanFactory){

        //代理service
        GenericBeanDefinition serviceProxyBD = new GenericBeanDefinition();
        serviceProxyBD.setBeanClass(ServiceProxyProcessor.class);
        registry.registerBeanDefinition("serviceProxy", serviceProxyBD);

        //创建并替换原事务拦截器
        GenericBeanDefinition interceptorBD = new GenericBeanDefinition();
        interceptorBD.setBeanClass(PreTransactionInterceptor.class);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        TransactionAttributeSource attr = beanFactory.getBean(TransactionAttributeSource.class);
        propertyValues.add("transactionAttributeSource", attr);
        interceptorBD.setPropertyValues(propertyValues);
        registry.registerBeanDefinition("preInterceptor", interceptorBD);

        PreTransactionInterceptor interceptor = beanFactory.getBean(PreTransactionInterceptor.class);
        BeanFactoryTransactionAttributeSourceAdvisor sourceAdvisor = beanFactory.getBean(BeanFactoryTransactionAttributeSourceAdvisor.class);
        sourceAdvisor.setAdvice(interceptor);
        dataSource = beanFactory.getBean(MultipleDataSource.class);

        //注入本地数据属性加载器
        GenericBeanDefinition localLoader = new GenericBeanDefinition();
        localLoader.setBeanClass(LocalPropertiesLoader.class);
        PropertiesParser<?extends DataSourceProperties> properParser = null;
        try {
            properParser = beanFactory.getBean(PropertiesParser.class);
        }catch (Exception bex){
            logger.info("not configuration customer datasource properties parser ");
        }

        if(properParser == null){
            properParser = new DefaultDataSourcePropertiesParser((ConfigurableEnvironment) environment);
        }
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        constructorArgumentValues.addGenericArgumentValue(properParser);
        localLoader.setConstructorArgumentValues(constructorArgumentValues);
        registry.registerBeanDefinition("localPropertiesLoader",localLoader);

        //加载数据源配置
        loadDatasourceProperties(beanFactory);

        //注入databaseId scanner
        GenericBeanDefinition databaseIdScanner = new GenericBeanDefinition();
        databaseIdScanner.setBeanClass(BindDatabaseScanner.class);
        MutablePropertyValues scannerValues = new MutablePropertyValues();
        databaseIdScanner.setPropertyValues(scannerValues);
        String share = environment.getProperty(SHARE_DEFAULT, "true");
        boolean datasourceShareDefault=Boolean.valueOf(share);
        String tenantEnableStr = environment.getProperty(TENANT_ENABLE, "true");
        boolean tenantEnable=Boolean.valueOf(tenantEnableStr);
        scannerValues.add("registry", registry);
        scannerValues.add("sourceProperties", sourceProperties);
        scannerValues.add("datasourceShareDefault", datasourceShareDefault);
        scannerValues.add("tenantEnable", tenantEnable);
        registry.registerBeanDefinition("bindDatabaseScanner",databaseIdScanner);

    }

    private void loadDatasourceProperties(ListableBeanFactory beanFactory) {

        logger.info("loadDatasourceProperties ");
        Map<String, DatasourcePropertiesLoader> beansOfType = beanFactory.getBeansOfType(DatasourcePropertiesLoader.class);
        propertiesLoader = new CompositePropertiesLoader(beansOfType.values());
        MultipleSourceProperties  parse = propertiesLoader.loadProperties();
        sourceProperties.setDatasourceProperties(parse.getDatasourceProperties());
        sourceProperties.setDefaultDatabaseId(parse.getDefaultDatabaseId());
        RouteContextManager.setMultipleSourceProperties(sourceProperties);
    }

    private void initDatasource() {
        if (logger.isDebugEnabled()) {
            logger.debug("initDatasource");
        }
        Map<String, DataSource> dataSources = new HashMap<>(sourceProperties.getDatasourceProperties().size());
        Map<String, DataSourceProperties> datasourceProperties = sourceProperties.getDatasourceProperties();
        for (Map.Entry<String, DataSourceProperties> entry : datasourceProperties.entrySet()) {
            String dataId = entry.getKey();
            DataSourceProperties value = entry.getValue();
            DataSource dataSource = dataSourceResolver.createDataSource(value);
            dataSources.put(dataId, dataSource);
        }
        dataSource.setDataSources(dataSources);
        registryPlatformTransactionManager();

    }


    private void registryPlatformTransactionManager() {
        logger.info("registryPlatformTransactionManager ");
        Map<String, DataSourceProperties> datasourceProperties = sourceProperties.getDatasourceProperties();
        datasourceProperties.forEach((databaseId, properties) -> {
            if (properties.isMaster()) {
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(DataSourceTransactionManager.class);
                MutablePropertyValues propertyValues = new MutablePropertyValues();
                propertyValues.add("dataSource", dataSource.getDatasource(databaseId));
                beanDefinition.setPropertyValues(propertyValues);
                registry.registerBeanDefinition(TRANSACTION_MANAGER_PREFIX + databaseId, beanDefinition);
            }
        });

    }


    @Override
    public void afterSingletonsInstantiated() {
        try {
            BeanFactory factory = (BeanFactory) registry;
            BindDatabaseScanner scanner = factory.getBean(BindDatabaseScanner.class);
            scanner.scanServiceBindDatabaseIds();

        } catch (Exception e) {
            logger.error("scanServiceBindDatabaseIds failure ", e);
        }
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
