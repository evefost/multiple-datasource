
package com.eve.multiple.datasource;

import com.eve.multiple.RouteContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author xieyang
 */
public class DynamicDataSource extends AbstractDataSource {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean isMaster;

    private String url;

    private String databaseId;

    private DataSource delegate;

    public DynamicDataSource(DataSource delegate) {
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
        if (logger.isDebugEnabled()) {
            logger.debug("get connect [{}][{}][{}]({})", isMaster ? "master" : "slaver", databaseId, url, trans ? "has transaction" : "no transaction");
        }
        return delegate.getConnection();
    }

    @Override
    public Connection getConnection(String s, String s1) throws SQLException {
        throw new UnsupportedOperationException("Not supported by MultipleDataSource");
    }
}
