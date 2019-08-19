
package com.eve.multiple.datasource;


import com.eve.multiple.DynamicDatasourceException;
import com.eve.multiple.RouteContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据源简单的包装类
 * <p>
 * @author xieyang
 */
public class DataSourceWrapper extends AbstractDataSource implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean isMaster;

    private String url;

    private String databaseId;

    private DataSource delegate;

    public DataSourceWrapper(DataSource delegate) {
        this.delegate = delegate;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }


    @Override
    public Connection getConnection() throws SQLException {
        boolean trans = RouteContextManager.hasTransaction();
        if (logger.isTraceEnabled()) {
            logger.trace("get connect [{}][{}][{}]({})", isMaster ? "master" : "slaver", databaseId, url, trans ? "has transaction" : "no transaction");
        }
        return delegate.getConnection();
    }

    @Override
    public Connection getConnection(String s, String s1) {
        throw new UnsupportedOperationException("Not supported by MultipleDataSource");
    }

    @Override
    public void close() throws IOException {
        if (delegate instanceof Closeable) {
            Closeable closeable = (Closeable) delegate;
            closeable.close();
        } else {
            logger.warn("cannot close database[{}] because is not implement Closeable", delegate.getClass().getName());
            throw new DynamicDatasourceException("cannot close database " + databaseId + " because is not implement Closeable");
        }
    }
}
