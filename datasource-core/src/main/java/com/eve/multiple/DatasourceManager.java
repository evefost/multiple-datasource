package com.eve.multiple;


import com.eve.multiple.datasource.MultipleDataSource;
import com.eve.multiple.event.DatasourceConfigChangeEvent;
import com.eve.multiple.event.StatementBuildFinishEvent;
import com.eve.multiple.properties.BaseDataSourceProperties;
import com.eve.multiple.properties.DatasourcePropertiesLoader;
import com.eve.multiple.properties.MultipleSourceProperties;
import com.eve.multiple.properties.SourceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.*;

import static com.eve.multiple.SourceType.*;


/**
 * 数据源管理器
 *
 * @author xieyang
 * @date 19/7/28
 */
public class DatasourceManager implements ApplicationListener<ApplicationEvent>, InitializingBean {

    public static final String TRANSACTION_MANAGER_PREFIX = "transactionManager_";

    public static final String DATABASE_ID_APPEND = "_";
    private static volatile boolean refreshIng;
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private BeanDefinitionRegistry registry;

    private DatasourcePropertiesLoader propertiesLoader;

    private DataSourceResolver dataSourceResolver;

    private volatile SourceProperties<MultipleSourceProperties, BaseDataSourceProperties> sourceProperties;

    private MultipleDataSource multipleDataSource;

    private Set<String> transactionManagerNames = new HashSet<>();

    private static Map<String, BaseDataSourceProperties> datasourceProperties;

    public static void checkRefresh() throws InterruptedException {
        if (refreshIng) {
            synchronized (DatasourceManager.class) {
                while (DatasourceManager.refreshIng) {
                    DatasourceManager.class.wait();
                }
            }
        }
    }

    public void setPropertiesLoader(DatasourcePropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
    }

    public void setRegistry(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public void setDataSourceResolver(DataSourceResolver dataSourceResolver) {
        this.dataSourceResolver = dataSourceResolver;
    }

    public SourceProperties<MultipleSourceProperties, BaseDataSourceProperties> getSourceProperties() {
        return sourceProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof DatasourceConfigChangeEvent) {
            logger.info("receive datasource config change");
            try {
                refreshDatasource();
            } catch (Exception e) {
                logger.warn("refresh datasource failure", e);
            }
        } else if (event instanceof StatementBuildFinishEvent) {
            StatementBuildFinishEvent buildFinishEvent = (StatementBuildFinishEvent) event;
            BeanFactory factory = (BeanFactory) registry;
            multipleDataSource = factory.getBean(MultipleDataSource.class);
            loadDatasourceProperties();
            initDatasource();
            logger.info("scan bind database id");
            BindDatabaseScanner scanner = factory.getBean(BindDatabaseScanner.class);
            Collection<DatabaseMeta> methodMetas = new ArrayList<>();
            Collection<DatabaseMeta> statementMetas = null;
            try {
                Map<Method, MethodDatabase> methodMappers = scanner.scanServiceBindDatabaseIds();
                methodMappers.forEach((k, v) -> {
                    methodMetas.add(v.getDatabaseMeta());
                });
                Map<String, DatabaseMeta> statementMappers = scanner.scanMapperDatabaseIds(buildFinishEvent.getConfiguration());
                statementMetas = statementMappers.values();
            } catch (Exception e) {
                logger.error("scanServiceBindDatabaseIds failure ", e);
            }

            try {
                logger.info("check bind database .. ");
                checkBindDatabase(methodMetas);
                checkBindDatabase(statementMetas);
            } catch (Throwable e) {
                logger.error("绑定的数据源可能不正确 {}", e.getMessage());
            }
        }
    }


    /**
     * 检测当前绑定的数据源是否正确
     *
     * @param metas
     */
    private void checkBindDatabase(Collection<DatabaseMeta> metas) {
        if (metas == null || metas.isEmpty()) {
            return;
        }
        Map<String, SourceProperties<SourceProperties, BaseDataSourceProperties>> multipleProperties = sourceProperties.getMultipleProperties();
        Set<String> keys = multipleProperties.keySet();
        //选中一个租户来测试
        String tenantId = null;
        for (String key : keys) {
            if (!key.equals(PropertiesSource.DEFAULT)) {
                tenantId = key;
                break;
            }
        }
        RouteContextManager.setCurrentTenant(tenantId);
        try {
            for (DatabaseMeta meta : metas) {
                SourceType sourceType = meta.getSourceType();
                if (SHARE_DEFAULT.equals(sourceType) || TENANT_DEFAULT.equals(sourceType)) {
                    sourceProperties.getDefaultDatabaseProperties(meta);
                } else if (SHARE.equals(sourceType) || TENANT.equals(sourceType)) {
                    String databaseId = meta.getDatabaseId();
                    sourceProperties.getProperties(databaseId, meta);
                }
            }
        } finally {
            RouteContextManager.setCurrentTenant(null);
        }
    }

    private void refreshDatasource() throws InterruptedException {
        logger.info("refresh datasource");
        //该操作应阻塞所有新进入请求，刷新完成后放行
        synchronized (DatasourceManager.class) {
            refreshIng = true;
            //需等待未完成的请求处理完再destroy
            Thread.sleep(2000);
            long s = System.currentTimeMillis();
            try {
                destroy();
                loadDatasourceProperties();
                initDatasource();
                long e = System.currentTimeMillis();
                logger.info("datasource refresh time:{}", (e - s));
                DatasourceManager.class.notifyAll();
            } finally {
                refreshIng = false;
            }
        }
    }

    private void destroy() {
        logger.info("destroying datasource ");
        multipleDataSource.close();
        transactionManagerNames.forEach((name) -> {
            registry.removeBeanDefinition(name);
        });
    }

    private void loadDatasourceProperties() {
        logger.info("loadDatasourceProperties ");
        SourceProperties<MultipleSourceProperties, BaseDataSourceProperties> loadProperties = propertiesLoader.loadProperties();
        sourceProperties = loadProperties;
        RouteContextManager.setMultipleSourceProperties(sourceProperties);
    }


    private void initDatasource() {
        logger.debug("initDatasource ... ");
        datasourceProperties = sourceProperties.getDatasourceProperties();
        Map<String, DataSource> dataSources = new HashMap<>(datasourceProperties.size());
        for (Map.Entry<String, BaseDataSourceProperties> entry : datasourceProperties.entrySet()) {
            String databaseId = entry.getKey();
            BaseDataSourceProperties properties = entry.getValue();
            if (shouldCreateDatasource(properties)) {
                DataSource dataSource = dataSourceResolver.createDataSource(properties);
                dataSources.put(databaseId, dataSource);
            }
        }
        multipleDataSource.setDataSources(dataSources);
        registryPlatformTransactionManager();

    }

    public static BaseDataSourceProperties getDatabasePropties(String databaseId) {
        return datasourceProperties.get(databaseId);
    }

    private boolean shouldCreateDatasource(BaseDataSourceProperties properties) {
        if (properties.getUrl() != null && properties.getUsername() != null && properties.getPassword() != null) {
            return true;
        }
        return false;
    }

    private void registryPlatformTransactionManager() {
        logger.info("registryPlatformTransactionManager ");
        transactionManagerNames.clear();
        datasourceProperties.forEach((databaseId, properties) -> {
            if (properties.isMaster()) {
                String transactionManagerName = TRANSACTION_MANAGER_PREFIX + databaseId;
                transactionManagerNames.add(transactionManagerName);
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(DataSourceTransactionManager.class);
                MutablePropertyValues propertyValues = new MutablePropertyValues();
                propertyValues.add("dataSource", multipleDataSource.getDatasource(databaseId));
                beanDefinition.setPropertyValues(propertyValues);
                registry.registerBeanDefinition(transactionManagerName, beanDefinition);
            }
        });

    }

    public DataSourceTransactionManager getTransactionManager(){
        String databaseId = RouteContextManager.currentDatabaseId();
        String transactionManagerName = TRANSACTION_MANAGER_PREFIX + databaseId;
        BeanFactory beanFactory = (BeanFactory) registry;
       return beanFactory.getBean(transactionManagerName,DataSourceTransactionManager.class);
    }




    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
