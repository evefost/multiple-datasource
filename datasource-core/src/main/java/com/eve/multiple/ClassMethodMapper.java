package com.eve.multiple;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 类说明
 * <p>
 *
 * @author xieyang
 * @date 2019/8/15
 */
public class ClassMethodMapper {
    private String className;

    private Map<Method, MethodDatabase> methodDatabaseMapping = new HashMap<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void put(Method method, MethodDatabase database) {
        methodDatabaseMapping.put(method, database);
    }

    public MethodDatabase getDatabase(Method method) {
        return methodDatabaseMapping.get(method);
    }

    public Map<Method, MethodDatabase> getMethodDatabaseMapping() {
        return methodDatabaseMapping;
    }

}
