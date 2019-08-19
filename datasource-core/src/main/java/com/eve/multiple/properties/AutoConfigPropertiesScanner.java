package com.eve.multiple.properties;


import com.eve.multiple.DynamicDatasourceException;
import com.eve.multiple.PropertiesSource;
import com.eve.multiple.annotation.DatabaseProperties;
import com.eve.multiple.annotation.JdbcUrl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 数据源属性注入扫描实现
 * @author xieyang
 * @date 19/8/2
 */

public class AutoConfigPropertiesScanner implements AutoConfigScanner {

    private ConfigurableListableBeanFactory beanFactory;


    public AutoConfigPropertiesScanner(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 扫描所有被注解 @AsDatabaseProperties
     * 标识的实实例
     *
     * @author xieyang
     * @date 19/8/2
     */
    @Override
    public SourceProperties<SourceProperties, BaseDataSourceProperties> scanProperties() throws ClassNotFoundException, IllegalAccessException {

        MultipleSourceProperties<BaseDataSourceProperties> multipleSourceProperties = new MultipleSourceProperties<BaseDataSourceProperties>();
        multipleSourceProperties.setSource(PropertiesSource.DEFAULT);
        Map<String, BaseDataSourceProperties> propertiesMap = new HashMap<>();
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String defaultDatabaseId = null;
        String defaultSourceMethod = null;
        for (String beanName : beanDefinitionNames) {

            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }
            Class<?> targetClass = classLoader.loadClass(beanClassName);
            Method[] declaredMethods = targetClass.getDeclaredMethods();
            for (Method m : declaredMethods) {
                DatabaseProperties dbAnnotation = AnnotationUtils.findAnnotation(m, DatabaseProperties.class);
                if (dbAnnotation != null) {
                    String prpBeanName = m.getName();
                    BaseDataSourceProperties properties = (BaseDataSourceProperties) beanFactory.getBean(prpBeanName);
                    String databaseId = dbAnnotation.value();
                    if (StringUtils.isEmpty(databaseId)) {
                        databaseId = dbAnnotation.databaseId();
                    }
                    if (StringUtils.isEmpty(databaseId)) {
                        databaseId = properties.getId();
                    } else {
                        properties.setId(databaseId);
                    }
                    if (StringUtils.isEmpty(databaseId)) {
                        String location = targetClass.getName() + "." + m.getName();
                        throw new DynamicDatasourceException("[" + location + "]数据源属性没有配置 database id");
                    }
                    String masterId = dbAnnotation.masterId();
                    properties.setParentId(StringUtils.isEmpty(masterId) ? null : masterId);
                    if (dbAnnotation.isDefault()) {
                        properties.setDefault(true);
                        if (defaultDatabaseId == null) {
                            defaultDatabaseId = databaseId;
                            defaultSourceMethod = targetClass.getName() + "." + m.getName();
                        } else {
                            String location = targetClass.getName() + "." + m.getName();
                            throw new DynamicDatasourceException("不可以指定多数默认数据源[" + defaultSourceMethod + "]&[" + location + "]");
                        }
                    }
                    Class<? extends BaseDataSourceProperties> prpClass = properties.getClass();
                    Field[] declaredFields = prpClass.getDeclaredFields();
                    for (Field f : declaredFields) {
                        f.setAccessible(true);
                        if (f.isAnnotationPresent(JdbcUrl.class)) {
                            String url = (String) f.get(properties);
                            properties.setUrl(url);
                        }
                    }
                    checkBaseProperties(properties);
                    propertiesMap.put(databaseId, properties);
                }
            }

        }

        multipleSourceProperties.setDatasourceProperties(propertiesMap);
        if (defaultDatabaseId != null) {
            multipleSourceProperties.setDefaultDatabaseId(defaultDatabaseId);
        }
        processMasterSlaverRelations(propertiesMap);
        return multipleSourceProperties;
    }


    private void processMasterSlaverRelations(Map<String, BaseDataSourceProperties> propertiesMap) {
        Set<Map.Entry<String, BaseDataSourceProperties>> entries = propertiesMap.entrySet();
        for (Map.Entry<String, BaseDataSourceProperties> entry : entries) {
            BaseDataSourceProperties value = entry.getValue();
            if (!StringUtils.isEmpty(value.getParentId())) {
                String parentId = value.getParentId();
                BaseDataSourceProperties masterProperties = propertiesMap.get(parentId);
                if (masterProperties == null) {
                    throw new DynamicDatasourceException("[" + value.getId() + "]指定的主库[" + parentId + "]在不存在，请检查配置");
                }
                if (!StringUtils.isEmpty(masterProperties.getParentId())) {
                    throw new DynamicDatasourceException("[" + parentId + "]是一个从库，不能作为[" + value.getId() + "]的主库");
                }
                masterProperties.addSlaver(value);
            }
        }
    }

    /**
     * 简单检测数据源配置信息是否正确
     * @param properties BaseDataSourceProperties
     */
    private void checkBaseProperties(BaseDataSourceProperties properties) {
        if (StringUtils.isEmpty(properties.getUrl())) {
            throw new DynamicDatasourceException("数据源url 没有配置");
        }
        if (StringUtils.isEmpty(properties.getUsername())) {
            throw new DynamicDatasourceException("数据源username没有配置");
        }
        if (StringUtils.isEmpty(properties.getPassword())) {
            throw new DynamicDatasourceException("数据源password没有配置");
        }
    }

}
