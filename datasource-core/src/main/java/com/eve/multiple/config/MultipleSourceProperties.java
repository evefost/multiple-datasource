package com.eve.multiple.config;

import java.util.Map;

/**
 * @author 谢洋
 */

public class MultipleSourceProperties {

    private String defaultDatabaseId;

    private Map<String, DataSourceProperties> datasourceProperties;

    public Map<String, DataSourceProperties> getDatasourceProperties() {
        return datasourceProperties;
    }

    public void setDatasourceProperties(Map<String, DataSourceProperties> datasourceProperties) {
        this.datasourceProperties = datasourceProperties;
    }


    public String getDefaultDatabaseId() {
        return defaultDatabaseId;
    }

    public void setDefaultDatabaseId(String defaultDatabaseId) {
        this.defaultDatabaseId = defaultDatabaseId;
    }
}
