

package com.eve.multiple.interceptor;



import com.eve.multiple.DatabaseMeta;
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
        DatabaseMeta database = RouteContextManager.getDatabase(invocation.getMethod());
        if(database == null){
            database = RouteContextManager.getDefaultDatabase();
        }
        boolean master = RouteContextManager.isMaster(database);
        if(master){
            RouteContextManager.setCurrentDatabase(database, true);
        }else {
            //自动切到主库
            DatabaseMeta masterDatabase = RouteContextManager.getMaster(database);
            RouteContextManager.setCurrentDatabase(masterDatabase, true);
            if (logger.isDebugEnabled()) {
                logger.debug("slaver[{}] switch to master[{}](has transaction)", database, masterDatabase);
            }
        }

        try {
            return super.invoke(invocation);
        } finally {
            RouteContextManager.setCurrentDatabase(null, true);
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
