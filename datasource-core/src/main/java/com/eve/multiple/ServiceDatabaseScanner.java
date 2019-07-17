package com.eve.multiple;


import com.eve.multiple.annotation.Database;
import com.eve.multiple.config.DataSourceProperties;
import com.eve.multiple.config.DatasourceConfig;
import com.eve.multiple.config.MultipleSourceProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 */
public class ServiceDatabaseScanner {

    private BeanDefinitionRegistry registry;

    private MultipleSourceProperties sourceProperties;

    public ServiceDatabaseScanner(BeanDefinitionRegistry registry, MultipleSourceProperties sourceProperties) {
        this.registry = registry;
        this.sourceProperties = sourceProperties;
    }

    public void scanServiceBindDatabaseIds() throws ClassNotFoundException {
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        Map<Method, DatasourceConfig.MethodMapping> methodMappingMap = new HashMap<>();
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
                    throw new RuntimeException(beanClass.getName() + " and " + ifc.getName() + " bind with database");
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

    private void parseMethodDatabaseId(String classDatabaseId, Class<?> clz, Map<Method, DatasourceConfig.MethodMapping> methodMappingMap) {
        if (classDatabaseId != null) {
            DataSourceProperties dps = sourceProperties.getDatasourceProperties().get(classDatabaseId);
            if (dps == null) {
                throw new RuntimeException(clz.getName() + "bind databaseId:" + classDatabaseId + "not exist");
            }
        }
        Method[] methods = clz.getDeclaredMethods();
        for (Method m : methods) {
            DatasourceConfig.MethodMapping methodMapping = new DatasourceConfig.MethodMapping();
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

}
