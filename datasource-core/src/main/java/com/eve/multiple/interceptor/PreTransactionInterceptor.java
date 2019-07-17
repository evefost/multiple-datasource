package com.eve.multiple.interceptor;


import com.eve.multiple.RouteContextManager;
import com.eve.multiple.config.DatasourceConfig;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 *
 * @author xieyang
 */
public class PreTransactionInterceptor extends TransactionInterceptor {
    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        int increase = RouteContextManager.increase(true);
        if (increase == 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("enter transaction interceptor ");
            }
        }
        String databaseId = RouteContextManager.getDatabaseId(invocation.getMethod());
        if(databaseId == null){
            databaseId = RouteContextManager.getDefaultDatabaseId();
        }
        boolean master = RouteContextManager.isMaster(databaseId);
        if(master){
            RouteContextManager.setCurrentDatabaseId(databaseId, true);
        }else {
            //自动切到主库
            String masterId = RouteContextManager.getMasterId(databaseId);
            RouteContextManager.setCurrentDatabaseId(masterId, true);
            if (logger.isDebugEnabled()) {
                logger.debug("slaver[{}] switch to master[{}](has transaction)", databaseId, masterId);
            }
        }

        try {
            return super.invoke(invocation);
        } finally {
            RouteContextManager.setCurrentDatabaseId(null, true);
            int decrease = RouteContextManager.decrease(true);
            if (decrease == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("out of interceptor ");
                }
            }
        }
    }

    @Override
    protected PlatformTransactionManager determineTransactionManager(TransactionAttribute txAttr) {
        String managerName = DatasourceConfig.TRANSACTION_MANAGER_PREFIX + RouteContextManager.currentDatabaseId();
        Object bean = getBeanFactory().getBean(managerName);
        if (bean != null) {
            return (PlatformTransactionManager) bean;
        }
        return super.determineTransactionManager(txAttr);
    }
}
