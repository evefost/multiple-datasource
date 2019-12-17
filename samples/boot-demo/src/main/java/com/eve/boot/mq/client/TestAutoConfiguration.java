package com.eve.boot.mq.client;

import com.eve.boot.mq.client.annotation.AsRabbitmqProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * 类说明
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/10/12
 */
@Configuration
@Order(HIGHEST_PRECEDENCE)
public class TestAutoConfiguration {

    @ConfigurationProperties(prefix = "rabbitmq")
    @AsRabbitmqProperties(containerFactory = "container_1", templateName = "aaa")
    public RabbitmqProperties rabbitmqProperties() {
        return new RabbitmqProperties();
    }


}
