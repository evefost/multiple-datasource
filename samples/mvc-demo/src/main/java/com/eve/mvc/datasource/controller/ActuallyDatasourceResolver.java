package com.eve.mvc.datasource.controller;

import com.eve.multiple.DataSourceResolver;
import com.eve.multiple.config.DataSourceProperties;
import com.eve.multiple.datasource.DynamicDataSource;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

/**
 * @author 谢洋
 */
public class ActuallyDatasourceResolver implements DataSourceResolver {

    @Override
    public DataSource createDataSource(DataSourceProperties properties) {
        BasicDataSource delegate = new BasicDataSource();
        delegate.setDriverClassName("com.mysql.jdbc.Driver");
        delegate.setUrl(properties.getUrl());
        delegate.setUsername(properties.getUsername());
        delegate.setPassword(properties.getPassword());
        DynamicDataSource dynamicDataSource = new DynamicDataSource(delegate);
        dynamicDataSource.setDatabaseId(properties.getId());
        dynamicDataSource.setUrl(properties.getUrl());
        dynamicDataSource.setMaster(properties.getParentId() == null);
        return dynamicDataSource;
    }
}
