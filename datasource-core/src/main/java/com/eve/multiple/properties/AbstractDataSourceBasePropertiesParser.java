
package com.eve.multiple.properties;

import com.eve.multiple.PropertiesSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.*;


/**
 * 固定格式，多主多从基础配置属性解释器
 * @author xieyang
 */
public abstract class AbstractDataSourceBasePropertiesParser<DP extends BaseDataSourceProperties> implements PropertiesParser, EnvironmentAware {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String ID = "id";

    private static final String MASTER_PREFIX = "datasource.master";

    private static final String SLAVER_PREFIX = "datasource.slaver";

    private static final String DEFAULT_DATABASE_ID = "datasource.default.ds-id";

    /**
     * key:databaseId
     */
    protected Map<String, DP> datasourceProperties;

    protected String defaultDatabaseId;

    protected Map<String, String> properties;

    private ConfigurableEnvironment environment;

    public AbstractDataSourceBasePropertiesParser() {
    }

    public AbstractDataSourceBasePropertiesParser(ConfigurableEnvironment environment) {
        this.environment = environment;

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;

    }

    private Map<String, String> findProperties() {
        MutablePropertySources propertySources = environment.getPropertySources();
        List<EnumerablePropertySource> propertySourceList = new ArrayList<>();
        //(x)->x*
        propertySources.forEach(propertySource -> {
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource ps = (EnumerablePropertySource) propertySource;
                propertySourceList.add(ps);
            }
        });
        Map<String, String> properties = new HashMap<>();
        propertySourceList.forEach(ps -> {
            String[] propertyNames = ps.getPropertyNames();
            for (String propertyName : propertyNames) {
                if (propertyName.startsWith(MASTER_PREFIX) || propertyName.startsWith(SLAVER_PREFIX) || propertyName.equals(DEFAULT_DATABASE_ID)) {
                    properties.put(propertyName, (String) ps.getProperty(propertyName));
                }
            }
        });
        return properties;

    }

    @Override
    public MultipleSourceProperties<DP> parse() {
        properties = findProperties();
        Map<String/*databaseId*/, /*databaseKeyPrefix*/String> masterKey = new HashMap<>(10);
        Map<String/*databaseId*/, /*databaseKeyPrefix*/String> slaverKey = new HashMap<>(10);
        properties.forEach((key, value) -> {
            //datasource.master.ds0-id:datasource.master.ds0
            if (key.startsWith(MASTER_PREFIX) && key.endsWith(ID)) {
                masterKey.put(value, key.substring(0, key.length() - 3));
            } else if (key.startsWith(SLAVER_PREFIX) && key.endsWith(ID)) {
                slaverKey.put(value, key.substring(0, key.length() - 3));
            } else if (key.equals(DEFAULT_DATABASE_ID)) {
                defaultDatabaseId = value;
            }

        });
        //主从分开属性解释
        Map<String, DP> masters = new HashMap<>(masterKey.size());
        processProperties(masterKey, masters);
        Map<String, DP> slavers = new HashMap<>(slaverKey.size());
        processProperties(slaverKey, slavers);
        //整合多套一主多从数据
        masters.forEach((mDataId, mProperites) -> {
            slavers.forEach((sDataId, sProperites) -> {
                if (mDataId.equals(sProperites.getParentId())) {
                    mProperites.addSlaver(sProperites);
                }
            });
        });
        datasourceProperties = masters;
        slavers.forEach((k, v) -> {
            datasourceProperties.put(k, v);
        });
        if (datasourceProperties == null || datasourceProperties.isEmpty()) {
            return null;
        }
        MultipleSourceProperties<DP> multipleSourceProperties = new MultipleSourceProperties();
        multipleSourceProperties.setSource(PropertiesSource.DEFAULT);
        multipleSourceProperties.setDatasourceProperties(datasourceProperties);
        multipleSourceProperties.setDefaultDatabaseId(defaultDatabaseId);
        return multipleSourceProperties;
    }


    public Map<String, DP> getDatasourceProperties() {
        return datasourceProperties;
    }

    public String getDefaultDatabaseId() {
        return defaultDatabaseId;
    }

    private void processProperties(Map<String, String> dataSourceKey, Map<String, DP> result) {
        for (Map.Entry<String, String> entry : dataSourceKey.entrySet()) {
            String dataId = entry.getKey();
            String dataPrefix = entry.getValue();

            DP dataSourceProperties = null;
            try {
                dataSourceProperties = getPropertiesClass().newInstance();
            } catch (Throwable e) {
                logger.error("创建数据源属性实例出错", e);
                throw new RuntimeException("创建数据源属性实例出错");
            }
            dataSourceProperties.setId(dataId);
            Set<Map.Entry<String, String>> entries = properties.entrySet();
            for (Map.Entry<String, String> entry2 : entries) {
                String key = entry2.getKey();
                String value = entry2.getValue();
                if (key.startsWith(dataPrefix) && key.endsWith("url")) {
                    dataSourceProperties.setUrl(value);
                } else if (key.startsWith(dataPrefix) && key.endsWith("username")) {
                    dataSourceProperties.setUsername(value);
                } else if (key.startsWith(dataPrefix) && key.endsWith("password")) {
                    dataSourceProperties.setPassword(value);
                } else if (key.startsWith(dataPrefix) && key.endsWith("masterId")) {
                    dataSourceProperties.setParentId(value);
                } else {
                    if (key.startsWith(dataPrefix)) {
                        processCustomerProperties(dataSourceProperties, key, value);
                    }

                }
            }
            result.put(dataId, dataSourceProperties);
        }
    }

    /**
     * 获取实际使用的 继承的 BaseDataSourceProperties 的类型
     *
     * @return
     */
    protected abstract Class<DP> getPropertiesClass();


    /**
     * 用户可据实际情况，自定义属性
     * etc:
     * if (propertyKey.startsWith(propertyKeyPrefix) && propertyKey.endsWith("xxx")) {
     * dataSourceProperties.setUrl(propertyValue);
     * }
     *
     * @param dataSourceProperties 某个数据源属性
     * @param propertyKey          属性key
     * @param propertyValue
     */
    protected abstract void processCustomerProperties(DP dataSourceProperties, String propertyKey, String propertyValue);


    protected boolean match(String propertyKey, String propertyKeySuffix) {
        return propertyKey.endsWith(propertyKeySuffix);
    }

}
