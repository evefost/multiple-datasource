package com.eve.multiple.interceptor;

import com.eve.multiple.RouteContextManager;
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
 * {@link Executor}
 * @author Administrator
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
public class ExecutorInterceptor implements Interceptor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ExecutorInterceptor() {
    }


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        calculateDatasource(ms);
        RouteContextManager.setMapStatement(ms);
        try {
            return invocation.proceed();
        } finally {
            RouteContextManager.removeMapStatement();
        }
    }

    private void calculateDatasource(MappedStatement ms){
        String databaseId = RouteContextManager.currentDatabaseId();
        String msBindId = ms.getDatabaseId();
        String currentId = databaseId;
        if(msBindId != null){
            currentId = msBindId;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} bind database [{}]", ms.getId(), msBindId);
        }
        String masterId =  null;
        if(RouteContextManager.isMaster(currentId)){
            masterId =  currentId;

        }else {
            masterId =  RouteContextManager.getMasterId(currentId);
        }
        String slaverId = RouteContextManager.getSlaverId(masterId);
        boolean isSelect  =  SqlCommandType.SELECT.equals(ms.getSqlCommandType());
        String tip = "";
        if(RouteContextManager.hasTransaction()){
            RouteContextManager.setCurrentDatabaseId(masterId,true);
            tip = "has transaction ";
        }else {
            if(!RouteContextManager.hadUpdateBefore() && isSelect){
                //查询操作且之前没有过更新操作
                RouteContextManager.setCurrentDatabaseId(slaverId==null?masterId:slaverId,false);
                tip = "no transaction ";
            }else {
                RouteContextManager.setCurrentDatabaseId(masterId,false);
                if(isSelect){
                    if (logger.isDebugEnabled()) {
                        logger.debug("no transaction query，but had update before,so switch to master:[{}]", masterId);
                    }
                    tip = "no transaction,but had update before";
                } else {
                    tip = "no transaction update ";
                }
            }
        }
        boolean master = RouteContextManager.isMaster(RouteContextManager.currentDatabaseId());
        if(!isSelect){
            RouteContextManager.markUpdateOperateFlag();
        }

        if (logger.isDebugEnabled()) {
            logger.info("[{}] should connect to [{}][{}]({})", ms.getSqlCommandType(), master ? "master" : "slaver", RouteContextManager.currentDatabaseId(), tip);
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof RoutingStatementHandler) {
            target = new DynamicRoutingStatementHandler((RoutingStatementHandler) target);
        }
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
