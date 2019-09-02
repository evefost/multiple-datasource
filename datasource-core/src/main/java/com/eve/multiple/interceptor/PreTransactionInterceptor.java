

package com.eve.multiple.interceptor;


import com.eve.multiple.DatabaseMeta;
import com.eve.multiple.RouteContextManager;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import static com.eve.multiple.DatasourceManager.TRANSACTION_MANAGER_PREFIX;


/**
 * spring 事务拦截器
 * <p>
 *
 * @author xieyang
 * @date 2019/7/26
 */
public class PreTransactionInterceptor extends TransactionInterceptor {
    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        int increase = RouteContextManager.increase(true);
        if (increase == 1) {
            if (logger.isTraceEnabled()) {
                logger.trace("enter transaction interceptor ");
            }
        }
        Class<?> targetClass = invocation.getThis().getClass();
        DatabaseMeta database = RouteContextManager.getDatabase(targetClass, invocation.getMethod());
        boolean master = RouteContextManager.isMaster(database);
        if (master) {
            RouteContextManager.setCurrentDatabase(database, true);
        } else {
            //自动切到主库
            DatabaseMeta masterDatabase = RouteContextManager.getMaster(database);
            RouteContextManager.setCurrentDatabase(masterDatabase, true);
            if (logger.isTraceEnabled()) {
                logger.trace("slaver[{}] switch to master[{}](has transaction)", database, masterDatabase);
            }
        }

        try {
            return super.invoke(invocation);
        } finally {
            RouteContextManager.setCurrentDatabase(null, true);
            int decrease = RouteContextManager.decrease(true);
            if (decrease == 0 && logger.isTraceEnabled()) {
                logger.trace("out of interceptor ");
            }
        }
    }

    /**
     * @param txAttr txAttr
     * @author xieyang
     * @date 2019/7/26
     * @return TransactionManager
     */
    @Override
    protected PlatformTransactionManager determineTransactionManager(TransactionAttribute txAttr) {
        String managerName = TRANSACTION_MANAGER_PREFIX + RouteContextManager.currentDatabaseId();
        Object bean = getBeanFactory().getBean(managerName);
        if (bean != null) {
            return (PlatformTransactionManager) bean;
        }
        return super.determineTransactionManager(txAttr);
    }
}
