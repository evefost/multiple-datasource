package com.eve.multiple;

import com.eve.multiple.interceptor.ServiceInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Service;

import java.lang.reflect.Proxy;

/**
 *
 * @author 谢洋
 */
public class ServiceProxyProcessor implements BeanPostProcessor, BeanFactoryAware {

    private ClassLoader classLoader = ServiceProxyProcessor.class.getClassLoader();

    private BeanDefinitionRegistry registry;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?>[] interfaces = findServiceInterfaces(beanName);
        if (interfaces!= null) {
            return Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, new ServiceInterceptor(bean,interfaces));
        }
        return bean;
    }

    private Class<?>[] findServiceInterfaces(String beanName){

        boolean contain = registry.containsBeanDefinition(beanName);
        if(!contain){
            return null;
        }
        BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
        if(beanDefinition.getBeanClassName() == null){
            return null;
        }
        try {
            Class<?> clz = classLoader.loadClass(beanDefinition.getBeanClassName());
            if (clz.isAnnotationPresent(Service.class)) {
                return clz.getInterfaces();
            }
            Class<?>[] interfaces = clz.getInterfaces();
            for (Class<?> ifc : interfaces) {
                if (ifc.isAnnotationPresent(Service.class)) {
                    return interfaces;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        registry= (BeanDefinitionRegistry) beanFactory;
    }
}
