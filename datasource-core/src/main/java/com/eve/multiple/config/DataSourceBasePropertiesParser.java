package com.eve.multiple.config;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xieyang
 */
public abstract class DataSourceBasePropertiesParser<DP extends DataSourceProperties> {

    private final String ID = "id";

    private final String MASTER_PREFIX = "datasource.master";

    private final String SLAVER_PREFIX = "datasource.slaver";

    private final String DEFAULT_DATABASE_ID = "datasource.default.ds-id";

    private ConfigurableEnvironment environment;


    /**
     * key:databaseId
     */
    protected Map<String, DP> datasourceProperties;

    protected String defaultDatabaseId;

    protected Map<String, String> properties;

    public DataSourceBasePropertiesParser() {
    }

    public DataSourceBasePropertiesParser(ConfigurableEnvironment environment) {
        this.environment = environment;

    }

    public void setEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    private Map<String, String> findProperties() {
        MutablePropertySources propertySources = environment.getPropertySources();
        List<EnumerablePropertySource> propertySourceList = new ArrayList<>();
        propertySources.forEach((v) -> {
            if (v instanceof EnumerablePropertySource) {
                EnumerablePropertySource ps = (EnumerablePropertySource) v;
                propertySourceList.add(ps);
            }
        });
        Map<String, String> properties = new HashMap<>();
        propertySourceList.forEach((ps) -> {
            String[] propertyNames = ps.getPropertyNames();
            for (String propertyName : propertyNames) {
                if (propertyName.startsWith(MASTER_PREFIX) || propertyName.startsWith(SLAVER_PREFIX) || propertyName.equals(DEFAULT_DATABASE_ID)) {
                    properties.put(propertyName, (String) ps.getProperty(propertyName));
                }
            }
        });
        return properties;

    }

    public void parse() throws InstantiationException, IllegalAccessException {
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
    }


    public Map<String, DP> getDatasourceProperties() {
        return datasourceProperties;
    }

    public String getDefaultDatabaseId() {
        return defaultDatabaseId;
    }

    private void processProperties(Map<String, String> dataSourceKey, Map<String, DP> result) throws IllegalAccessException, InstantiationException {
          for (Map.Entry<String, String> entry : dataSourceKey.entrySet()) {
            String dataId = entry.getKey();
            String dataPrefix = entry.getValue();

            DP dataSourceProperties = getPropetiesClass().newInstance();
            dataSourceProperties.setId(dataId);
            properties.forEach((key, value) -> {
                if (key.startsWith(dataPrefix) && key.endsWith("url")) {
                    dataSourceProperties.setUrl(value);
                } else if (key.startsWith(dataPrefix) && key.endsWith("username")) {
                    dataSourceProperties.setUsername(value);
                } else if (key.startsWith(dataPrefix) && key.endsWith("password")) {
                    dataSourceProperties.setPassword(value);
                } else if (key.startsWith(dataPrefix) && key.endsWith("masterId")) {
                    dataSourceProperties.setParentId(value);
                }else {
                    if (key.startsWith(dataPrefix)) {
                        processCustomerProperties(dataSourceProperties,key,value);
                    }

                }
            });
            result.put(dataId, dataSourceProperties);
        }
    }

    protected abstract Class<DP> getPropetiesClass();


    /**
     *  自定义属性
     *  etc:
     *   if (propertyKey.startsWith(propertyKeyPrefix) && propertyKey.endsWith("xxx")) {
     *   dataSourceProperties.setUrl(propertyValue);
     *    }
     * @param dataSourceProperties 某个数据源属性
     * @param propertyKey 属性key
     * @param propertyValue
     */
    protected abstract void processCustomerProperties(DP dataSourceProperties,String propertyKey,String propertyValue);



    protected boolean match(String propertyKey,String propertyKeySuffix){
         return propertyKey.endsWith(propertyKeySuffix);
    }

}
