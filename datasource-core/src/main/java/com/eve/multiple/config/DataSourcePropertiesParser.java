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
public class DataSourcePropertiesParser {

    private final String ID = "id";

    private final String MASTER_PREFIX = "datasource.master";

    private final String SLAVER_PREFIX = "datasource.slaver";

    private final String DEFAULT_DATABASE_ID = "datasource.default.ds-id";

    private ConfigurableEnvironment environment;


    private Map<String/*databaseId*/, DataSourceProperties> datasourceProperties;

    private String defaultDatabaseId;

    private Map<String, String> properties;

    public DataSourcePropertiesParser(ConfigurableEnvironment environment) {
        this.environment = environment;
        properties = findProperties();
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

    public void parse() {

        Map<String, String> masterKey = new HashMap<>(10);
        Map<String, String> slaverKey = new HashMap<>(10);
        properties.forEach((key, value) -> {
            if (key instanceof String && value instanceof String) {
                if (key.startsWith(MASTER_PREFIX) && key.endsWith(ID)) {
                    masterKey.put(value, key.substring(0, key.length() - 3));
                } else if (key.startsWith(SLAVER_PREFIX) && key.endsWith(ID)) {
                    slaverKey.put(value, key.substring(0, key.length() - 3));
                } else if (key.equals(DEFAULT_DATABASE_ID)) {
                    defaultDatabaseId = value;
                }
            }
        });
        //主从分开属性解释
        Map<String, DataSourceProperties> masters = new HashMap<>(masterKey.size());
        processProperties(masterKey, masters);
        Map<String, DataSourceProperties> slavers = new HashMap<>(slaverKey.size());
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


    public Map<String, DataSourceProperties> getDatasourceProperties() {
        return datasourceProperties;
    }

    private void processProperties(Map<String, String> dataSourceKey, Map<String, DataSourceProperties> result) {
        for (Map.Entry<String, String> entry : dataSourceKey.entrySet()) {
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            String dataPrefix = entry.getValue();
            String dataId = entry.getKey();
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
                }
            });
            result.put(dataId, dataSourceProperties);
        }
        ;
    }

    public String getDefaultDatabaseId() {
        return defaultDatabaseId;
    }
}
