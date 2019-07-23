package com.eve.multiple;


import com.eve.multiple.annotation.Database;
import com.eve.multiple.config.DataSourceProperties;
import com.eve.multiple.config.MultipleSourceProperties;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 */
public class BindDatabaseScanner {

    private BeanDefinitionRegistry registry;

    private MultipleSourceProperties sourceProperties;


    public BindDatabaseScanner(BeanDefinitionRegistry registry, MultipleSourceProperties sourceProperties) {
        this.registry = registry;
        this.sourceProperties = sourceProperties;

    }

    public void scanServiceBindDatabaseIds() throws ClassNotFoundException {
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        Map<Method, MethodMapping> methodMappingMap = new HashMap<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String beanName : beanDefinitionNames) {

            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition.getBeanClassName() == null) {
                continue;
            }
            Class<?> beanClass = classLoader.loadClass(beanDefinition.getBeanClassName());
            if (beanClass.isAnnotationPresent(Service.class)) {
                String classDatabaseId = findDatabaseIdOnClass(beanClass);
                parseMethodDatabaseId(classDatabaseId, beanClass, methodMappingMap);
                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> ifc : interfaces) {
                    parseMethodDatabaseId(classDatabaseId, ifc, methodMappingMap);
                }
            }
        }

        RouteContextManager.setMethodDatabaseMapping(methodMappingMap);
    }

    private String findDatabaseIdOnClass(Class<?> beanClass) {
        Database annotation = null;
        if (beanClass.isAnnotationPresent(Database.class)) {
            Class<?>[] interfaces = beanClass.getInterfaces();
            for (Class<?> ifc : interfaces) {
                if (ifc.isAnnotationPresent(Database.class)) {
                    Database clzDb = beanClass.getAnnotation(Database.class);
                    Database ifcDb = ifc.getAnnotation(Database.class);
                    if (!clzDb.value().equals(ifcDb.value())) {
                        throw new RuntimeException(beanClass.getName() + " bind database[" + clzDb.value() + "] and " + ifc.getName() + "  bind  databases[" + ifcDb.value() + "] are different");
                    }
                }
            }
            annotation = beanClass.getAnnotation(Database.class);
        } else {
            Class<?>[] interfaces = beanClass.getInterfaces();
            for (Class<?> ifc : interfaces) {
                if (ifc.isAnnotationPresent(Database.class)) {
                    annotation = ifc.getAnnotation(Database.class);
                }
            }
        }
        if (annotation == null) {
            return null;
        }
        return annotation.value();
    }

    private void parseMethodDatabaseId(String classDatabaseId, Class<?> clz, Map<Method,MethodMapping> methodMappingMap) {
        if (classDatabaseId != null) {
            DataSourceProperties dps = sourceProperties.getDatasourceProperties().get(classDatabaseId);
            if (dps == null) {
                throw new RuntimeException(clz.getName() + " bind databaseId:" + classDatabaseId + " is not exist");
            }
        }
        Method[] methods = clz.getDeclaredMethods();
        for (Method m : methods) {
            MethodMapping methodMapping = new MethodMapping();
            if (m.isAnnotationPresent(Database.class)) {
                String methodDatabaseId = m.getAnnotation(Database.class).value();
                if (methodDatabaseId != null) {
                    checkDatabaseExist(methodDatabaseId, clz, m);
                }
                methodMapping.setDatabaseId(methodDatabaseId);
            } else {
                methodMapping.setDatabaseId(classDatabaseId);
            }
            methodMapping.setMethod(m);
            methodMappingMap.put(m, methodMapping);
        }
    }


    private void checkDatabaseExist(String databaseId, Class clz, Method method) {
        DataSourceProperties dataSourceProperties = sourceProperties.getDatasourceProperties().get(databaseId);
        if (dataSourceProperties == null) {
            throw new RuntimeException(clz.getName() + method.getName() + "bind databaseId:" + databaseId + " not exist");
        }
    }


    public static void scanMapperDatabaseIds(Configuration configuration) throws IllegalAccessException, NoSuchFieldException {
        Collection<Class<?>> mappers = configuration.getMapperRegistry().getMappers();
        Field databaseField = MappedStatement.class.getDeclaredField("databaseId");
        databaseField.setAccessible(true);
        for (Class clzz : mappers) {
            String clzDatabaseId = null;
            if (clzz.isAnnotationPresent(Database.class)) {
                clzDatabaseId = ((Database) clzz.getAnnotation(Database.class)).value();
            }
            Method[] methods = clzz.getMethods();
            for (Method m : methods) {
                String id = clzz.getName() + "." + m.getName();
                MappedStatement stm = configuration.getMappedStatement(id);
                if (stm == null) {
                    continue;
                }
                if (clzDatabaseId != null) {
                    databaseField.set(stm, clzDatabaseId);
                }
                if (m.isAnnotationPresent(Database.class)) {
                    String databaseId = m.getAnnotation(Database.class).value();
                    databaseField.set(stm, databaseId);
                }

            }

        }
    }


    public static class MethodMapping {

        private Method method;

        private String databaseId;

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public String getDatabaseId() {
            return databaseId;
        }

        public void setDatabaseId(String databaseId) {
            this.databaseId = databaseId;
        }
    }

}
