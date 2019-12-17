package com.eve.boot.common;

import java.lang.reflect.Method;

public class PropertiesInfo<P> {

    private Method method;

    private P properties;

    private String beanName;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public P getProperties() {
        return properties;
    }

    public void setProperties(P properties) {
        this.properties = properties;
    }
}

