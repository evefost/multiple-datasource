
package com.eve.multiple;

import com.eve.multiple.datasource.MultipleDataSource;
import org.apache.ibatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import static org.springframework.util.Assert.notNull;

/**
 * 自定义事务实例
 * @author xieyang
 */
public class SpringDynamicDatasourceTransaction implements Transaction {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DataSource dataSource;

    private Connection connection;

    private boolean isConnectionTransactional;

    private boolean autoCommit;

    public SpringDynamicDatasourceTransaction(DataSource dataSource) {
        notNull(dataSource, "No DataSource specified");
        this.dataSource = dataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (this.connection == null) {
            openConnection();
        }
        return this.connection;
    }

    /**
     * Gets a connection from Spring transaction manager and discovers if this
     * {@code Transaction} should manage connection or let it to Spring.
     * <p>
     * It also reads autocommit setting because when using Spring Transaction MyBatis
     * thinks that autocommit is always false and will always call commit/rollback
     * so we need to no-op that calls.
     */
    private void openConnection() throws SQLException {
        this.connection = DataSourceUtils.getConnection(getDataSource());
        this.autoCommit = this.connection.getAutoCommit();
        this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.connection, getDataSource());

        if (logger.isTraceEnabled()) {
            logger.trace(
                    "JDBC Connection ["
                            + this.connection
                            + "] will"
                            + (this.isConnectionTransactional ? " " : " not ")
                            + "be managed by Spring");
        }
    }

    private DataSource getDataSource() {
        if (dataSource instanceof MultipleDataSource) {
            String databaseId = RouteContextManager.currentDatabaseId();
            MultipleDataSource multipleDataSource = (MultipleDataSource) this.dataSource;
            DataSource ds = multipleDataSource.getDatasource(databaseId);
            if (ds == null) {
                DatabaseMeta databaseMeta = RouteContextManager.currentDatabase();
                Set<String> dbIds = multipleDataSource.getDataSources().keySet();
                logger.error("没找到数据源:[{}] :datasourceList[{}]", databaseId, databaseMeta, dbIds);
                throw new DynamicDatasourceException("没找到数据源:[" + databaseId + "]");
            }
            return ds;
        }
        return this.dataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws SQLException {
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            if (logger.isTraceEnabled()) {
                logger.trace("Committing JDBC Connection [" + this.connection + "]");
            }
            this.connection.commit();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() throws SQLException {
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            if (logger.isDebugEnabled()) {
                logger.debug("Rolling back JDBC Connection [" + this.connection + "]");
            }
            this.connection.rollback();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SQLException {
        DataSourceUtils.releaseConnection(this.connection, getDataSource());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getTimeout() throws SQLException {

        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(getDataSource());
        if (holder != null && holder.hasTimeout()) {
            return holder.getTimeToLiveInSeconds();
        }
        return null;
    }

}
