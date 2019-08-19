package com.eve.mvc.datasource.controller;

import com.eve.multiple.DataSourceResolver;
import com.eve.multiple.datasource.DataSourceWrapper;
import com.eve.multiple.properties.BaseDataSourceProperties;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

/**
 * @author xieyang
 */
public class ActuallyDatasourceResolver implements DataSourceResolver<DataSource,BaseDataSourceProperties> {

    @Override
    public DataSource createDataSource(BaseDataSourceProperties properties) {
        BasicDataSource delegate = new BasicDataSource();
        delegate.setDriverClassName("com.mysql.jdbc.Driver");
        delegate.setUrl(properties.getUrl());
        delegate.setUsername(properties.getUsername());
        delegate.setPassword(properties.getPassword());
        DataSourceWrapper dynamicDataSource = new DataSourceWrapper(delegate);
        dynamicDataSource.setDatabaseId(properties.getId());
        dynamicDataSource.setUrl(properties.getUrl());
        dynamicDataSource.setMaster(properties.getParentId() == null);
        return dynamicDataSource;
    }
}
