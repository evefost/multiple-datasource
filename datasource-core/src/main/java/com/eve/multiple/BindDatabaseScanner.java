
package com.eve.multiple;


import com.eve.multiple.annotation.Database;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static com.eve.multiple.SourceType.*;


/**
 * @author xieyang
 */
public class BindDatabaseScanner {

    private static Logger logger = LoggerFactory.getLogger(BindDatabaseScanner.class);

    private static final SourceType DEFAULT_TYPE = SHARE_DEFAULT;

    private BeanDefinitionRegistry registry;

    public void setRegistry(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }


    public Map<Method, MethodDatabase> scanServiceBindDatabaseIds() throws ClassNotFoundException {
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Map<String, ClassMethodMapper> classMethodMapperMapping = new HashMap<>();
        for (String beanName : beanDefinitionNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition.getBeanClassName() == null) {
                continue;
            }
            Class<?> beanClass = classLoader.loadClass(beanDefinition.getBeanClassName());
            if (beanClass.isAnnotationPresent(Service.class)) {
                ClassMethodMapper methodMapper = new ClassMethodMapper();
                methodMapper.setClassName(beanClass.getName());
                Database classDatabase = findDatabaseAnnotation(beanClass);
                checkDatabaseAnnotation(classDatabase, beanClass, null);
                parseMethodDatabaseId(classDatabase, beanClass, null, methodMapper);
                List<Class<?>> interfaces = ClassUtils.findAllInterface(beanClass);
                for (Class<?> ifc : interfaces) {
                    parseMethodDatabaseId(classDatabase, ifc, beanClass, methodMapper);
                }
                classMethodMapperMapping.put(methodMapper.getClassName(), methodMapper);
            }
        }
        RouteContextManager.setClassMethodMapperMapping(classMethodMapperMapping);
        Map<Method, MethodDatabase> methodMappingMap = new HashMap<>();
        Set<Map.Entry<String, ClassMethodMapper>> entries = classMethodMapperMapping.entrySet();
        for (Map.Entry<String, ClassMethodMapper> entry : entries) {
            ClassMethodMapper value = entry.getValue();
            Map<Method, MethodDatabase> methodDatabaseMapping = value.getMethodDatabaseMapping();
            methodMappingMap.putAll(methodDatabaseMapping);
        }
        return methodMappingMap;
    }


    private Database findDatabaseAnnotation(Class<?> beanClass) {
        Database annotation = null;
        if (beanClass.isAnnotationPresent(Database.class)) {
            List<Class<?>> interfaces = ClassUtils.findAllInterface(beanClass);
            for (Class<?> ifc : interfaces) {
                if (ifc.isAnnotationPresent(Database.class)) {
                    Database clzDb = beanClass.getAnnotation(Database.class);
                    Database ifcDb = ifc.getAnnotation(Database.class);
                    if (!clzDb.value().equals(ifcDb.value())) {
                        throw new DynamicDatasourceException(beanClass.getName() + " bind database[" + clzDb.value() + "] and " + ifc.getName() + "  bind  databases[" + ifcDb.value() + "] are different");
                    }
                }
            }
            annotation = beanClass.getAnnotation(Database.class);
        } else {
            List<Class<?>> interfaces = ClassUtils.findAllInterface(beanClass);
            for (Class<?> ifc : interfaces) {
                if (ifc.isAnnotationPresent(Database.class)) {
                    annotation = ifc.getAnnotation(Database.class);
                }
            }
        }
        return annotation;
    }

    private void parseMethodDatabaseId(Database classDatabase, Class<?> clzz, Class<?> targetClass, ClassMethodMapper methodMapper) {
        String classDatabaseId = null;
        SourceType classSourceType = DEFAULT_TYPE;
        if (classDatabase != null) {
            classDatabaseId = StringUtils.isEmpty(classDatabase.value()) ? classDatabase.databaseId() : classDatabase.value();
            classSourceType = classDatabase.type();
        }
        Method[] methods = clzz.getDeclaredMethods();
        for (Method m : methods) {
            MethodDatabase methodMapping = new MethodDatabase();
            DatabaseMeta databaseMeta = new DatabaseMeta();
            methodMapping.setDatabaseMeta(databaseMeta);
            if (m.isAnnotationPresent(Database.class)) {
                Database methodDatabase = m.getAnnotation(Database.class);
                checkDatabaseAnnotation(classDatabase, clzz, m);
                String methodDatabaseId = methodDatabase.value();
                databaseMeta.setDatabaseId(methodDatabaseId);
                databaseMeta.setSourceType(methodDatabase.type());
            } else {
                databaseMeta.setDatabaseId(classDatabaseId);
                databaseMeta.setSourceType(classSourceType);
            }
            String paramsString = ClassUtils.methodParamsToString(m);
            if (targetClass == null) {
                String methodName = clzz.getSimpleName() + "." + m.getName() + paramsString;
                databaseMeta.setMethodName(methodName);
            } else {
                String methodName = targetClass.getSimpleName() + "." + m.getName() + paramsString;
                databaseMeta.setMethodName(methodName);
            }
            methodMapping.setMethod(m);
            methodMapper.put(m, methodMapping);
        }
    }


    public Map<String/*mapperId*/, DatabaseMeta> scanMapperDatabaseIds(Configuration configuration) throws IllegalAccessException, NoSuchFieldException {

        Collection<Class<?>> mappers = configuration.getMapperRegistry().getMappers();
        Field databaseField = MappedStatement.class.getDeclaredField("databaseId");
        databaseField.setAccessible(true);
        Map<String/*mapperId*/, DatabaseMeta> databaseMetaMap = new HashMap<>();
        for (Class<?> clzz : mappers) {
            String classDatabaseId = null;
            SourceType classSourceType = DEFAULT_TYPE;
            boolean clzExistAnno = false;
            if (clzz.isAnnotationPresent(Database.class)) {
                clzExistAnno = true;
                Database database = clzz.getAnnotation(Database.class);
                checkDatabaseAnnotation(database, clzz, null);
                classDatabaseId = StringUtils.isEmpty(database.value()) ? database.databaseId() : database.value();
                classSourceType = database.type();
            }
            Method[] methods = clzz.getMethods();
            for (Method m : methods) {
                String id = clzz.getName() + "." + m.getName();
                MappedStatement stm = null;
                try {
                    stm = configuration.getMappedStatement(id);
                } catch (Exception ex) {
                    logger.warn("[" + id + "]" + " not bind mapper statement ");
                }
                if (stm == null) {
                    continue;
                }

                String methodDatabaseId = classDatabaseId;
                SourceType methodType = classSourceType;
                boolean mtdExistAnno = false;
                if (m.isAnnotationPresent(Database.class)) {
                    mtdExistAnno = true;
                    Database methodDb = m.getAnnotation(Database.class);
                    checkDatabaseAnnotation(methodDb, clzz, m);
                    methodDatabaseId = StringUtils.isEmpty(methodDb.value()) ? methodDb.databaseId() : methodDb.value();
                    methodType = methodDb.type();
                    databaseField.set(stm, methodDatabaseId);
                }
                if (clzExistAnno || mtdExistAnno) {
                    String paramsString = ClassUtils.methodParamsToString(m);
                    DatabaseMeta databaseMeta = new DatabaseMeta();
                    String methodName = clzz.getSimpleName() + "." + m.getName() + paramsString;
                    databaseMeta.setMethodName(methodName);
                    databaseMeta.setDatabaseId(methodDatabaseId);
                    databaseMeta.setSourceType(methodType);
                    databaseMetaMap.put(stm.getId(), databaseMeta);
                }
            }
        }
        RouteContextManager.setStatementDatabaseMapping(databaseMetaMap);
        return databaseMetaMap;
    }


    private void checkDatabaseAnnotation(Database database, Class clzz, Method method) {
        if (database == null) {
            return;
        }
        String databaseId = StringUtils.isEmpty(database.value()) ? database.databaseId() : database.value();
        String location = clzz.getName();
        if (method != null) {
            location = clzz.getName() + "." + method.getName();
        }

        SourceType type = database.type();
        if (StringUtils.isEmpty(databaseId)) {
            if (type == null) {
                throw new DynamicDatasourceException("annotation value 和 type 不能同时为空 [" + location + "]");
            }
            if (SHARE.equals(type) || TENANT.equals(type)) {
                throw new DynamicDatasourceException("SHARE 或 TENANT 类型数据源 databaseId不能为空[" + location + "]");
            }
        } else {
            if (SHARE_DEFAULT.equals(type) || TENANT_DEFAULT.equals(type)) {
                throw new DynamicDatasourceException("SHARE_DEFAULT 或 TENANT_DEFAULT 类型数据源 databaseId必须为空值[" + location + "]");
            }
        }
    }


}
