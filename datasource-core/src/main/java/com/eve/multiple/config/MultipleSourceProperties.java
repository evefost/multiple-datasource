
package com.eve.multiple.config;

import java.util.Map;

/**
 * @author 谢洋
 */

public class MultipleSourceProperties<DP extends DataSourceProperties> {

    private String defaultDatabaseId;

    private Map<String, DP> datasourceProperties;

    public Map<String, DP> getDatasourceProperties() {
        return datasourceProperties;
    }

    public DP getProperties(String databaseId) {
        return datasourceProperties.get(databaseId);
    }

    public void setDatasourceProperties(Map<String, DP> datasourceProperties) {
        this.datasourceProperties = datasourceProperties;
    }


    public String getDefaultDatabaseId() {
        return defaultDatabaseId;
    }

    public DP getDefaultDatabaseProperties() {
        return datasourceProperties.get(defaultDatabaseId);
    }

    public void setDefaultDatabaseId(String defaultDatabaseId) {
        this.defaultDatabaseId = defaultDatabaseId;
    }
}
