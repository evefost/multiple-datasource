

package com.eve.multiple.interceptor;

import com.eve.multiple.RouteContextManager;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 *  StatementHandler
 * <p>
 *
 * @author xieyang
 * @date 2019/7/26
 */
public class DynamicRoutingStatementHandler implements StatementHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private StatementHandler delegate;

    public DynamicRoutingStatementHandler(StatementHandler delegate) {
        this.delegate = delegate;
    }


    @Override
    public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {

        String currentDatabaseId = RouteContextManager.currentDatabaseId();
        String url = connection.getMetaData().getURL();
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] actually use :[{}][{}]", RouteContextManager.getMapStatement().getId(), currentDatabaseId, url);
        }
        return delegate.prepare(connection, transactionTimeout);
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        delegate.parameterize(statement);
    }

    @Override
    public void batch(Statement statement) throws SQLException {
        delegate.batch(statement);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        return delegate.update(statement);
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        return delegate.query(statement, resultHandler);
    }

    @Override
    public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
        return delegate.queryCursor(statement);
    }

    @Override
    public BoundSql getBoundSql() {
        return delegate.getBoundSql();
    }

    @Override
    public ParameterHandler getParameterHandler() {
        return delegate.getParameterHandler();
    }
}
