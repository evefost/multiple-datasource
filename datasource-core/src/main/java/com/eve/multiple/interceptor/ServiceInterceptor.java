
package com.eve.multiple.interceptor;


import com.eve.multiple.DatabaseMeta;
import com.eve.multiple.DatasourceManager;
import com.eve.multiple.RouteContextManager;
import com.eve.multiple.ServiceProxyProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * service 层代理拦截器
 * <p>
 *
 * @author xieyang
 * @date 2019/7/26
 */
public class ServiceInterceptor implements InvocationHandler {

    private final static String METHOD_EQUALS = "equals";

    private final static String METHOD_HASHCODE = "hashCode";

    private final static String METHOD_TOSTRING = "toString";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Class<?> targetClass;

    private Object target;

    private List<Class<?>> interfaces;

    public ServiceInterceptor(ServiceProxyProcessor.TargetMapper targetMapper) {
        this.target = targetMapper.getTarget();
        this.interfaces = targetMapper.getInterfaces();
        this.targetClass = targetMapper.getTargetClass();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (METHOD_EQUALS.equals(method.getName())) {
            return method.equals(target);
        } else if (METHOD_HASHCODE.equals(method.getName())) {
            return method.hashCode();
        } else if (METHOD_TOSTRING.equals(method.getName())) {
            return method.toString();
        }
        DatasourceManager.checkRefresh();
        int increase = RouteContextManager.increase(false);
        if (increase == 1 && logger.isTraceEnabled()) {
            logger.trace("enter service {} >>>>>>>>>>>> ", method.getDeclaringClass());
        }
        DatabaseMeta database = RouteContextManager.getDatabase(targetClass, method);
        if (logger.isTraceEnabled()) {
            logger.trace("{}", database);
        }
        RouteContextManager.setCurrentDatabase(database, false);
        try {
            return method.invoke(target, args);
        } catch (Exception throwable) {
            throw findRootCause(throwable);
        } finally {
            RouteContextManager.setCurrentDatabase(null, false);
            int decrease = RouteContextManager.decrease(false);
            if (decrease == 0) {
                RouteContextManager.removeUpdateOperateFlag();
                if (logger.isTraceEnabled()) {
                    logger.trace("out of service {}<<<<<<<<<<<<<<", method.getDeclaringClass());
                }
            }
        }
    }

    private Throwable findRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }
        return findRootCause(cause);
    }

    @Override
    public String toString() {
        return "$Proxy$" + interfaces.get(0).getName();
    }
}