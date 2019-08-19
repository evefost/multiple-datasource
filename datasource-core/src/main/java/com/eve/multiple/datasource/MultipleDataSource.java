
package com.eve.multiple.datasource;

import com.eve.multiple.RouteContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 装数数据数据源
 * @author xieyang
 */
public class MultipleDataSource extends AbstractDataSource implements Closeable {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile Map<String, DataSource> dataSources;

    public DataSource getDatasource(String datasourceId) {
        return dataSources.get(datasourceId);
    }

    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }

    public synchronized void setDataSources(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public Connection getConnection() throws SQLException {
        String databaseId = RouteContextManager.currentDatabaseId();
        return dataSources.get(databaseId).getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Not supported by MultipleDataSource");
    }

    @Override
    public void close() {
        if (dataSources == null) {
            return;
        }
        dataSources.forEach((k, d) -> {
            if (d instanceof Closeable) {
                Closeable ds = (Closeable) d;
                try {
                    ds.close();
                } catch (Throwable e) {
                    logger.error("close database[{}] error ", k, e);
                }
            } else {
                logger.warn("cannot close database[{}] because is not implement Closeable ", k);
            }
        });
    }


}
