package com.eve.boot.mq.client;

import com.alibaba.fastjson.JSON;
import com.eve.boot.MqController;
import com.eve.common.entity.User;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 类说明
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/10/13
 */
@Component
public class MqListener {

    @RabbitListener(queues = "xie-queue", containerFactory = "container_1")
    @RabbitHandler
    public void listenerShoppeMq(User user) {
        System.out.println("收到消息==============" + JSON.toJSONString(user));
        MqController.message.put(user.getName(), user);
    }

}
