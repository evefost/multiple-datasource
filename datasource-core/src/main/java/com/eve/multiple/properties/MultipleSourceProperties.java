
package com.eve.multiple.properties;


import com.eve.multiple.DatabaseMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xieyang
 */

public class MultipleSourceProperties<DP extends BaseDataSourceProperties> implements SourceProperties {

    private String source;

    private String defaultDatabaseId;

    /**
     * key:databaseId
     */
    private Map<String, DP> datasourceProperties = new HashMap<>();


    @Override
    public String getSource() {
        return source;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public Map<String, DP> getDatasourceProperties() {
        return datasourceProperties;
    }

    public void setDatasourceProperties(Map<String, DP> datasourceProperties) {
        this.datasourceProperties = datasourceProperties;

    }

    @Override
    public DP getProperties(String databaseId, DatabaseMeta meta) {
        return datasourceProperties.get(databaseId);
    }

    @Override
    public String getDefaultDatabaseId() {
        return defaultDatabaseId;
    }

    @Override
    public void setDefaultDatabaseId(String defaultDatabaseId) {
        this.defaultDatabaseId = defaultDatabaseId;
    }

    @Override
    public DP getDefaultDatabaseProperties(DatabaseMeta meta) {
        return datasourceProperties.get(defaultDatabaseId);
    }

    @Override
    public void merge(SourceProperties sourceProperties) {
        Map<String, DP> mergeMap = sourceProperties.getDatasourceProperties();
        datasourceProperties.putAll(mergeMap);
    }

    @Override
    public String toString() {
        return "MultipleSourceProperties{" +
                "source='" + source + '\'' +
                ", defaultDatabaseId='" + defaultDatabaseId + '\'' +
                ", datasourceProperties=" + datasourceProperties +
                '}';
    }
}
