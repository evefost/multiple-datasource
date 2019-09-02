

package com.eve.multiple.interceptor;

import com.eve.multiple.DatabaseMeta;
import com.eve.multiple.RouteContextManager;
import com.eve.multiple.SourceType;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
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
public class LogStatementHandler implements StatementHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private StatementHandler delegate;

    public LogStatementHandler(StatementHandler delegate) {
        this.delegate = delegate;
    }


    @Override
    public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
        if (logger.isDebugEnabled()) {
            showLog(connection);
        }
        return delegate.prepare(connection, transactionTimeout);
    }

    private void showLog(Connection connection) throws SQLException {
        MappedStatement ms = RouteContextManager.getMapStatement();
        Log statementLog = ms.getStatementLog();
        DatabaseMeta databaseMeta = RouteContextManager.currentDatabase();
        SourceType sourceType = databaseMeta.getSourceType();
        String currentDatabaseId = RouteContextManager.currentDatabaseId();
        String url = connection.getMetaData().getURL();

        statementLog.debug("\nactually use :["+currentDatabaseId+"]["+url+"]");
        if(SourceType.TENANT.equals(sourceType)||SourceType.TENANT_DEFAULT.equals(sourceType)){
            statementLog.debug("==> Select Tenant["+RouteContextManager.getCurrentTenant()+"]");
        }
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
