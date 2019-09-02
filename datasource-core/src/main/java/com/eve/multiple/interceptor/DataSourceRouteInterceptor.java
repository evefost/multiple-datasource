
package com.eve.multiple.interceptor;


import com.eve.multiple.DatabaseMeta;
import com.eve.multiple.DatasourceManager;
import com.eve.multiple.RouteContextManager;
import com.eve.multiple.properties.BaseDataSourceProperties;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 该拦截在事务之后,获取连接之前
 * <p>
 * {@link Executor}
 *
 * @author xieyang
 * @date 2019/7/26
 */
@Intercepts({
        @Signature(
                type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        ),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
        )})
public class DataSourceRouteInterceptor implements Interceptor {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        determineTargetDataSource(ms);
        RouteContextManager.setMapStatement(ms);
        try {
            return invocation.proceed();
        } catch (Exception throwable) {
            String databaseId = RouteContextManager.currentDatabaseId();
            BaseDataSourceProperties propties = DatasourceManager.getDatabasePropties(databaseId);
            logger.error("访问数据库[{}] 失败: ", propties.getUrl(), throwable);
            throw throwable;
        } finally {
            RouteContextManager.removeMapStatement();
        }
    }

    /**
     * 处理数据源由路
     * @param ms mapperStatement
     */
    private void determineTargetDataSource(MappedStatement ms) {
        DatabaseMeta database = RouteContextManager.currentDatabase();
        DatabaseMeta msBindDb = RouteContextManager.getStatementDatabaseMeta(ms.getId());
        DatabaseMeta currentDatabase = database;
        if (msBindDb != null) {
            currentDatabase = msBindDb;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("mapper[{}]  database [{}]", ms.getId(), msBindDb);
        }
        DatabaseMeta master;
        if (RouteContextManager.isMaster(currentDatabase)) {
            master = currentDatabase;
        } else {
            master = RouteContextManager.getMaster(currentDatabase);
        }
        DatabaseMeta slaver = RouteContextManager.getSlaver(master);
        boolean isSelect = SqlCommandType.SELECT.equals(ms.getSqlCommandType());
        String tip;
        if (RouteContextManager.hasTransaction()) {
            RouteContextManager.setCurrentDatabase(master, true);
            tip = "has transaction ";
        } else {
            if (!RouteContextManager.hadUpdateBefore() && isSelect) {
                //查询操作且之前没有过更新操作
                RouteContextManager.setCurrentDatabase(slaver == null ? master : slaver, false);
                tip = "no transaction ";
            } else {
                RouteContextManager.setCurrentDatabase(master, false);
                if (isSelect) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("no transaction query，but had update before,so switch to master[{}]", master);
                    }
                    tip = "no transaction,but had update before";
                } else {
                    tip = "no transaction update ";
                }
            }
        }
        boolean isMaster = RouteContextManager.isMaster(RouteContextManager.currentDatabase());
        if (!isSelect) {
            RouteContextManager.markUpdateOperateFlag();
        }

        if (logger.isTraceEnabled()) {
            logger.trace("[{}] should connect to [{}][{}]({})", ms.getSqlCommandType(), isMaster ? "master" : "slaver", RouteContextManager.currentDatabase(), tip);
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof RoutingStatementHandler) {
            target = new LogStatementHandler((RoutingStatementHandler) target);
        }
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
