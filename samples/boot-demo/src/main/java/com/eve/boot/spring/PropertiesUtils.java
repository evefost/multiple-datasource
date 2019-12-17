package com.eve.boot.spring;

import com.eve.boot.common.PropertiesInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 类说明
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/10/14
 */
public class PropertiesUtils {


    public static <P, A extends Annotation> List<PropertiesInfo<P>> scanProperties(ConfigurableListableBeanFactory beanFactory, Class<P> t, Class<A> an) throws ClassNotFoundException, IllegalAccessException {
        List<PropertiesInfo<P>> list = new ArrayList<>();
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String beanName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }
            Class<?> targetClass = classLoader.loadClass(beanClassName);
            Method[] declaredMethods = targetClass.getDeclaredMethods();
            for (Method m : declaredMethods) {
                A dbAnnotation = AnnotationUtils.findAnnotation(m, an);
                if (dbAnnotation != null) {
                    String prpBeanName = m.getName();
                    P properties = (P) beanFactory.getBean(prpBeanName);
                    PropertiesInfo<P> propertiesInfo = new PropertiesInfo();
                    propertiesInfo.setMethod(m);
                    propertiesInfo.setProperties(properties);
                    propertiesInfo.setBeanName(prpBeanName);
                    list.add(propertiesInfo);
                }
            }
        }
        return list;
    }

}
