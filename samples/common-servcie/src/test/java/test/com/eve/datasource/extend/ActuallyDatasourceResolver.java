package test.com.eve.datasource.extend;

import com.eve.multiple.DataSourceResolver;
import com.eve.multiple.datasource.DataSourceWrapper;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author xieyang
 */
public class ActuallyDatasourceResolver implements DataSourceResolver<DataSourceWrapper,CustomerProperties> {

    @Override
    public DataSourceWrapper createDataSource(CustomerProperties properties) {
        HikariDataSource delegate = new HikariDataSource();
        delegate.setDriverClassName("com.mysql.jdbc.Driver");
        delegate.setJdbcUrl(properties.getUrl());
        delegate.setUsername(properties.getUsername());
        delegate.setPassword(properties.getPassword());
        DataSourceWrapper dynamicDataSource = new DataSourceWrapper(delegate);
        dynamicDataSource.setDatabaseId(properties.getId());
        dynamicDataSource.setUrl(properties.getUrl());
        dynamicDataSource.setMaster(properties.getParentId() == null);
        return dynamicDataSource;
    }
}
