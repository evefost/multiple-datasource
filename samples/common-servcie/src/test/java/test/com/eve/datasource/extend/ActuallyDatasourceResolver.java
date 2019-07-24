package test.com.eve.datasource.extend;

import com.eve.multiple.DataSourceResolver;
import com.eve.multiple.config.DataSourceProperties;
import com.eve.multiple.datasource.DynamicDataSource;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author 谢洋
 */
public class ActuallyDatasourceResolver implements DataSourceResolver<DynamicDataSource> {

    @Override
    public DynamicDataSource createDataSource(DataSourceProperties properties) {
        HikariDataSource delegate = new HikariDataSource();
        delegate.setDriverClassName("com.mysql.jdbc.Driver");
        delegate.setJdbcUrl(properties.getUrl());
        delegate.setUsername(properties.getUsername());
        delegate.setPassword(properties.getPassword());
        DynamicDataSource dynamicDataSource = new DynamicDataSource(delegate);
        dynamicDataSource.setDatabaseId(properties.getId());
        dynamicDataSource.setUrl(properties.getUrl());
        dynamicDataSource.setMaster(properties.getParentId() == null);
        return dynamicDataSource;
    }
}
