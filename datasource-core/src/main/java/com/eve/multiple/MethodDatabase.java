package com.eve.multiple;

import java.lang.reflect.Method;

/**
 * 方法数据源mapping
 * <p>
 *
 * @author xieyang
 * @date 2019/7/26
 */
public class MethodDatabase {

    private Method method;


    private DatabaseMeta databaseMeta;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public DatabaseMeta getDatabaseMeta() {
        return databaseMeta;
    }

    public void setDatabaseMeta(DatabaseMeta databaseMeta) {
        this.databaseMeta = databaseMeta;
    }
}
