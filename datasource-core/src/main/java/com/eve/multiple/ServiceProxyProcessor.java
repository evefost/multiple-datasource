

package com.eve.multiple;

import com.eve.multiple.interceptor.ServiceInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Service;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * service 层代理生成
 *
 * @author xieyang
 */
public class ServiceProxyProcessor implements BeanPostProcessor, BeanFactoryAware {
    private static Logger logger = LoggerFactory.getLogger(ServiceProxyProcessor.class);
    private ClassLoader classLoader = ServiceProxyProcessor.class.getClassLoader();

    private BeanDefinitionRegistry registry;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        TargetMapper targetMapper = findTargetMapper(beanName);
        if (targetMapper != null) {
            List<Class<?>> interfaces = targetMapper.getInterfaces();
            if (interfaces != null && interfaces.size() > 0) {
                targetMapper.setTarget(bean);
                Class<?>[] arr = interfaces.toArray(new Class[interfaces.size()]);
                return Proxy.newProxyInstance(this.getClass().getClassLoader(), arr, new ServiceInterceptor(targetMapper));
            }
        }
        return bean;
    }

    private TargetMapper findTargetMapper(String beanName) {

        boolean contain = registry.containsBeanDefinition(beanName);
        if (!contain) {
            return null;
        }
        BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
        if (beanDefinition.getBeanClassName() == null) {
            return null;
        }
        try {
            Class<?> clz = classLoader.loadClass(beanDefinition.getBeanClassName());
            if (clz.isAnnotationPresent(Service.class)) {
                TargetMapper targetMapper = new TargetMapper();
                targetMapper.setTargetClass(clz);
                List<Class<?>> allInterface = ClassUtils.findAllInterface(clz);
                targetMapper.setInterfaces(allInterface);
                return targetMapper;
            }
        } catch (Exception e) {
            logger.error("findServiceInterfaces exception", e);
        }
        return null;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        registry = (BeanDefinitionRegistry) beanFactory;
    }


    public static class TargetMapper {

        private List<Class<?>> interfaces;
        private Class<?> targetClass;
        private Object target;

        public Object getTarget() {
            return target;
        }

        public void setTarget(Object target) {
            this.target = target;
        }

        public List<Class<?>> getInterfaces() {
            return interfaces;
        }

        public void setInterfaces(List<Class<?>> interfaces) {
            this.interfaces = interfaces;
        }

        public Class<?> getTargetClass() {
            return targetClass;
        }

        public void setTargetClass(Class<?> targetClass) {
            this.targetClass = targetClass;
        }
    }

}
