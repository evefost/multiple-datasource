
package com.eve.multiple.config;


import com.eve.multiple.BindDatabaseScanner;
import com.eve.multiple.DataSourceResolver;
import com.eve.multiple.DatasourceManager;
import com.eve.multiple.ServiceProxyProcessor;
import com.eve.multiple.interceptor.PreTransactionInterceptor;
import com.eve.multiple.properties.*;
import org.apache.ibatis.executor.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * 配置类
 * <p>
 * {@link Executor}
 *
 * @author xieyang
 * @date 2019/7/26
 */
public class DatasourceConfig implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {


    public final Logger logger = LoggerFactory.getLogger(getClass());

    private BeanDefinitionRegistry registry;

    private DataSourceResolver dataSourceResolver;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setDataSourceResolver(DataSourceResolver dataSourceResolver) {
        this.dataSourceResolver = dataSourceResolver;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        registryBeanDefinitions(beanFactory);
    }


    private void registryBeanDefinitions(ConfigurableListableBeanFactory beanFactory) {

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

        //注入本地数据属性加载器
        GenericBeanDefinition localLoader = new GenericBeanDefinition();
        localLoader.setBeanClass(LocalPropertiesLoader.class);
        PropertiesParser<? extends BaseDataSourceProperties> properParser = null;
        try {
            properParser = beanFactory.getBean(PropertiesParser.class);
        } catch (BeansException bex) {
            logger.info("not configuration customer datasource properties parser ");
        }
        if (properParser == null) {
            properParser = new DefaultDataSourcePropertiesParser((ConfigurableEnvironment) environment, beanFactory);
        }
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        constructorArgumentValues.addGenericArgumentValue(properParser);
        localLoader.setConstructorArgumentValues(constructorArgumentValues);
        registry.registerBeanDefinition("localPropertiesLoader", localLoader);

        //注入databaseId scanner
        GenericBeanDefinition databaseIdScanner = new GenericBeanDefinition();
        databaseIdScanner.setBeanClass(BindDatabaseScanner.class);
        MutablePropertyValues scannerValues = new MutablePropertyValues();
        databaseIdScanner.setPropertyValues(scannerValues);
        scannerValues.add("registry", registry);
        registry.registerBeanDefinition("bindDatabaseScanner", databaseIdScanner);

        //组合数据源加载器
        DatasourcePropertiesLoader propertiesLoader = new CompositePropertiesLoader(beanFactory);

        //注入数据源管理器
        GenericBeanDefinition managerDb = new GenericBeanDefinition();
        managerDb.setBeanClass(DatasourceManager.class);
        MutablePropertyValues managerValues = new MutablePropertyValues();
        managerDb.setPropertyValues(managerValues);
        managerValues.add("registry", registry);
        managerValues.add("propertiesLoader", propertiesLoader);
        managerValues.add("dataSourceResolver", dataSourceResolver);
        registry.registerBeanDefinition("datasourceManager", managerDb);
    }

}
