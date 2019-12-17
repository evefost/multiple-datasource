package com.eve.boot.mq.client.config;


import com.eve.boot.common.PropertiesInfo;
import com.eve.boot.mq.client.RabbitmqProperties;
import com.eve.boot.mq.client.annotation.AsRabbitmqProperties;
import com.eve.boot.spring.PropertiesUtils;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 类说明
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/10/12
 */
@Component
public class RabbitMqInitPostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        List<PropertiesInfo<RabbitmqProperties>> propertiesInfos = null;
        try {
            propertiesInfos = PropertiesUtils.scanProperties(beanFactory, RabbitmqProperties.class, AsRabbitmqProperties.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        createMqClient(propertiesInfos);
    }


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    }


    public void createMqClient(List<PropertiesInfo<RabbitmqProperties>> propertiesInfos) {
        for (PropertiesInfo<RabbitmqProperties> propertiesInfo : propertiesInfos) {
            regiestRabbitMqClient(propertiesInfo);
        }

    }

    private void regiestRabbitMqClient(PropertiesInfo<RabbitmqProperties> propertiesInfo) {
        RabbitmqProperties properties = propertiesInfo.getProperties();
        Method method = propertiesInfo.getMethod();
        AsRabbitmqProperties annotation = AnnotationUtils.findAnnotation(method, AsRabbitmqProperties.class);
        String templateName = annotation.templateName();
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(properties.getAddress());
        connectionFactory.setUsername(properties.getUsername());
        connectionFactory.setPassword(properties.getPassword());
        connectionFactory.setVirtualHost(properties.getVirtualHost());
        connectionFactory.setPublisherConfirms(true);

        Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter();

        String containerFactoryName = annotation.containerFactory();
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setPrefetchCount(properties.getPrefetchCount());
        containerFactory.setConcurrentConsumers(properties.getConcurrency());
        containerFactory.setConnectionFactory(connectionFactory);
        containerFactory.setMessageConverter(messageConverter);

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        beanFactory.registerSingleton(templateName, template);
        beanFactory.registerSingleton(containerFactoryName, containerFactory);
    }


    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 10;
    }

}
