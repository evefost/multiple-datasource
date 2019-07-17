package com.eve.multiple.config;

import com.eve.multiple.DataSourceResolver;
import com.eve.multiple.RouteContextManager;
import com.eve.multiple.ServiceDatabaseScanner;
import com.eve.multiple.ServiceProxyProcessor;
import com.eve.multiple.datasource.MultipleDataSource;
import com.eve.multiple.interceptor.PreTransactionInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xieyang
 */
public class DatasourceConfig implements SmartInitializingSingleton, EnvironmentAware, BeanDefinitionRegistryPostProcessor {


    public final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String TRANSACTION_MANAGER_PREFIX = "transactionManager_";

    private BeanDefinitionRegistry registry;


    private MultipleSourceProperties sourceProperties = new MultipleSourceProperties();

    private ConfigurableEnvironment environment;

    private MultipleDataSource dataSource;

    private DataSourceResolver dataSourceResolver;

    public void setDataSourceResolver(DataSourceResolver dataSourceResolver) {
        this.dataSourceResolver = dataSourceResolver;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
        loadDatasourceProperties();
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

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
    }


    private void initDatasource() {
        Map<String, javax.sql.DataSource> dataSources = new HashMap<>(sourceProperties.getDatasourceProperties().size());
        Map<String, DataSourceProperties> datasourceProperties = sourceProperties.getDatasourceProperties();
        for (Map.Entry<String, DataSourceProperties> entry : datasourceProperties.entrySet()) {
            String dataId = entry.getKey();
            DataSourceProperties value = entry.getValue();
            DataSource dataSource = dataSourceResolver.createDataSource(value);
            dataSources.put(dataId, dataSource);
        }
        this.dataSource.setDataSources(dataSources);
    }

    private void loadDatasourceProperties() {
        logger.info("loadDatasourceProperties ");
        DataSourcePropertiesParser dataSourcePropertiesParser = new DataSourcePropertiesParser(environment);
        dataSourcePropertiesParser.parse();
        sourceProperties.setDatasourceProperties(dataSourcePropertiesParser.getDatasourceProperties());
        sourceProperties.setDefaultDatabaseId(dataSourcePropertiesParser.getDefaultDatabaseId());
        RouteContextManager.setMultipleSourceProperties(sourceProperties);
    }


    private void registryPlatformTransactionManager() {
        logger.info("registryPlatformTransactionManager ");
        Map<String, DataSourceProperties> datasourceProperties = sourceProperties.getDatasourceProperties();
        datasourceProperties.forEach((databaseId, properties) -> {
            if (properties.getParentId() == null) {
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
            ServiceDatabaseScanner scanner = new ServiceDatabaseScanner(registry, sourceProperties);
            scanner.scanServiceBindDatabaseIds();
        } catch (ClassNotFoundException e) {
            logger.error("scanServiceBindDatabaseIds failure ",e);
        }
        initDatasource();
        registryPlatformTransactionManager();
    }


    public static class MethodMapping {

        private Method method;

        private String databaseId;

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public String getDatabaseId() {
            return databaseId;
        }

        public void setDatabaseId(String databaseId) {
            this.databaseId = databaseId;
        }
    }
}
