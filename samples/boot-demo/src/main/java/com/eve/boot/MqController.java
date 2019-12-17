package com.eve.boot;


import com.alibaba.fastjson.JSON;
import com.eve.common.entity.User;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@RestController
public class MqController {

    public static Map<String, Object> message = new HashMap<>();
    @Resource(name = "aaa")
    RabbitTemplate template;


    @GetMapping("/publish")
    String publish(String key) {
        User user = new User();
        user.setAge(111);
        user.setName(key);
        template.convertAndSend("xie_test", "xie-rout-key", user);
        return "success";
    }

    @GetMapping("/publishGet")
    String publishGet(String key) {

        return JSON.toJSONString(message.get(key));
    }

}
