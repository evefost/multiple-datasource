
package com.eve.multiple.interceptor;


import com.eve.multiple.DatabaseMeta;
import com.eve.multiple.RouteContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Administrator
 */
public class ServiceInterceptor implements InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Object target;

    private Class<?>[] interfaces;


    public ServiceInterceptor(Object target, Class<?>[] interfaces) {
        this.target = target;
        this.interfaces = interfaces;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("equals".equals(method.getName())) {
            return method.equals(target);
        } else if ("hashCode".equals(method.getName())) {
            return method.hashCode();
        } else if ("toString".equals(method.getName())) {
            return method.toString();
        }

        int increase = RouteContextManager.increase(false);
        if (increase == 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("enter service {} >>>>>>>>>>>> ", method.getDeclaringClass());
            }
        }
        DatabaseMeta database = RouteContextManager.getDatabase(method);
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        if (database == null) {
            database = RouteContextManager.getDefaultDatabase();
            if (logger.isDebugEnabled()) {
                logger.debug("{} use default database [{}]", methodName, database);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("{} bind database [{}]", methodName, database);
            }
        }
        RouteContextManager.setCurrentDatabase(database, false);
        try {
            return method.invoke(target, args);
        } finally {
            RouteContextManager.setCurrentDatabase(null, false);
            int decrease = RouteContextManager.decrease(false);
            if (decrease == 0) {
                RouteContextManager.removeUpdateOperateFlag();
                if (logger.isDebugEnabled()) {
                    logger.debug("out of service {}<<<<<<<<<<<<<<", method.getDeclaringClass());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "$Proxy$" + interfaces[0].getName();
    }
}