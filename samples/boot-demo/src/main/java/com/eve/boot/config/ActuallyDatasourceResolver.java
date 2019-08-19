package com.eve.boot.config;

import com.eve.multiple.DataSourceResolver;
import com.eve.multiple.datasource.DataSourceWrapper;
import com.eve.multiple.properties.BaseDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;

/**
 *
 * @author xieyang
 */
public class ActuallyDatasourceResolver implements DataSourceResolver<DataSourceWrapper,BaseDataSourceProperties> {

    @Override
    public DataSourceWrapper createDataSource(BaseDataSourceProperties properties) {
        HikariDataSource delegate = new HikariDataSource();
        delegate.setDriverClassName("com.mysql.jdbc.Driver");
        delegate.setMaximumPoolSize(60);
        delegate.setConnectionTimeout(60000);
        delegate.setJdbcUrl(properties.getUrl());
        delegate.setUsername(properties.getUsername());
        delegate.setPassword(properties.getPassword());
        delegate.setMinimumIdle(5);
        DataSourceWrapper dynamicDataSource = new DataSourceWrapper(delegate);
        dynamicDataSource.setDatabaseId(properties.getId());
        dynamicDataSource.setUrl(properties.getUrl());
        dynamicDataSource.setMaster(properties.getParentId() == null);
        return dynamicDataSource;
    }
}
