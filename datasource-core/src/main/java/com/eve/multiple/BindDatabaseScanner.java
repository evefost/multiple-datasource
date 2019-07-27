
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
 * @author xieyang
 */
public class BindDatabaseScanner {




    private BeanDefinitionRegistry registry;

    private MultipleSourceProperties<DataSourceProperties> sourceProperties;

    private boolean datasourceShareDefault=true;

    private boolean tenantEnable=true;

    public BindDatabaseScanner() {


    }

    public BindDatabaseScanner(BeanDefinitionRegistry registry, MultipleSourceProperties sourceProperties) {
        this.registry = registry;
        this.sourceProperties = sourceProperties;
    }

    public void setRegistry(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public void setSourceProperties(MultipleSourceProperties<DataSourceProperties> sourceProperties) {
        this.sourceProperties = sourceProperties;
    }

    public void setDatasourceShareDefault(boolean datasourceShareDefault) {
        this.datasourceShareDefault = datasourceShareDefault;
    }

    public void setTenantEnable(boolean tenantEnable) {
        this.tenantEnable = tenantEnable;
    }

    public void scanServiceBindDatabaseIds() throws ClassNotFoundException {
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        Map<Method, MethodDatabase> methodMappingMap = new HashMap<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String beanName : beanDefinitionNames) {

            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition.getBeanClassName() == null) {
                continue;
            }
            Class<?> beanClass = classLoader.loadClass(beanDefinition.getBeanClassName());
            if (beanClass.isAnnotationPresent(Service.class)) {
                Database classDatabase = findDatabaseAnnotation(beanClass);
                parseMethodDatabaseId(classDatabase, beanClass, methodMappingMap);
                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> ifc : interfaces) {
                    parseMethodDatabaseId(classDatabase, ifc, methodMappingMap);
                }
            }
        }

        RouteContextManager.setMethodDatabaseMapping(methodMappingMap);
    }

    private Database findDatabaseAnnotation(Class<?> beanClass) {
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
        return annotation;
    }

    private void parseMethodDatabaseId(Database classDatabase, Class<?> clz, Map<Method, MethodDatabase> methodMappingMap) {
        String classDatabaseId = null;
        boolean classShare = datasourceShareDefault;
        if (classDatabase != null) {
            classDatabaseId = classDatabase.value();
            classShare = classDatabase.share();
        }
        if (classDatabaseId != null) {
            DataSourceProperties dps = sourceProperties.getProperties(classDatabaseId);
            if (dps == null) {
                throw new RuntimeException(clz.getName() + " bind databaseId:" + classDatabaseId + " is not exist");
            }
        }
        Method[] methods = clz.getDeclaredMethods();
        for (Method m : methods) {
            MethodDatabase methodMapping = new MethodDatabase();
            DatabaseMeta databaseMeta = new DatabaseMeta();
            methodMapping.setDatabaseMeta(databaseMeta);
            if (m.isAnnotationPresent(Database.class)) {
                Database methodDatabase = m.getAnnotation(Database.class);
                String methodDatabaseId = methodDatabase.value();
                if (methodDatabaseId != null) {
                    checkDatabaseExist(methodDatabaseId, clz, m);
                }
                databaseMeta.setShare(methodDatabase.share());
                databaseMeta.setDatabaseId(methodDatabaseId);
            } else {
                databaseMeta.setDatabaseId(classDatabaseId);
                databaseMeta.setShare(classShare);
            }
            if(!tenantEnable){
                databaseMeta.setShare(true);
            }
            methodMapping.setMethod(m);
            methodMappingMap.put(m, methodMapping);
        }
    }


    private void checkDatabaseExist(String databaseId, Class clz, Method method) {
        DataSourceProperties dataSourceProperties = sourceProperties.getProperties(databaseId);
        if (dataSourceProperties == null) {
            throw new RuntimeException(clz.getName() + method.getName() + "bind databaseId:" + databaseId + " not exist");
        }
    }


    public  void scanMapperDatabaseIds(Configuration configuration) throws IllegalAccessException, NoSuchFieldException {

        Collection<Class<?>> mappers = configuration.getMapperRegistry().getMappers();
        Field databaseField = MappedStatement.class.getDeclaredField("databaseId");
        databaseField.setAccessible(true);
        Map<String/*mapperId*/,DatabaseMeta> databaseMetaMap = new HashMap<>();
        for (Class<?> clzz : mappers) {
            String classDatabaseId = null;
            boolean classDatabaseShare = datasourceShareDefault;
            if (clzz.isAnnotationPresent(Database.class)) {
                Database database = clzz.getAnnotation(Database.class);
                classDatabaseId = database.value();
                classDatabaseShare = database.share();

            }
            Method[] methods = clzz.getMethods();
            for (Method m : methods) {
                String id = clzz.getName() + "." + m.getName();
                MappedStatement stm = configuration.getMappedStatement(id);
                if (stm == null) {
                    continue;
                }

                String methodDatabaseId = classDatabaseId;
                boolean methodShare = classDatabaseShare;
                if (m.isAnnotationPresent(Database.class)) {
                    Database methodDb = m.getAnnotation(Database.class);
                    methodDatabaseId = methodDb.value();
                    methodShare = methodDb.share();
                    databaseField.set(stm, methodDatabaseId);
                }

                if(methodDatabaseId != null){
                    databaseField.set(stm, methodDatabaseId);
                    DatabaseMeta databaseMeta = new DatabaseMeta();
                    databaseMeta.setDatabaseId(methodDatabaseId);
                    databaseMeta.setShare(methodShare);
                    databaseMetaMap.put(stm.getId(),databaseMeta);
                    if(!tenantEnable){
                        databaseMeta.setShare(true);
                    }
                }
            }

        }
        RouteContextManager.setStatementDatabaseMapping(databaseMetaMap);
    }


}
