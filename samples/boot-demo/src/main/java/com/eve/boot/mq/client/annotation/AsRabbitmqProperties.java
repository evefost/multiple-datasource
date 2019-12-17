package com.eve.boot.mq.client.annotation;

import com.eve.boot.annotation.AsProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;

import java.lang.annotation.*;

/**
 * 类说明
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/10/12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@AsProperties
@Bean
public @interface AsRabbitmqProperties {

    /**
     * The bean name of the {@link org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory}
     * to use to create the message listener container responsible to serve this endpoint.
     * <p>If not specified, the default container factory is used, if any.
     *
     * @return the {@link org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory}
     * bean name.
     */
    String containerFactory() default "";

    /**
     * The bean name of the {@link RabbitTemplate }
     *
     * @return
     */
    String templateName() default "";


}
