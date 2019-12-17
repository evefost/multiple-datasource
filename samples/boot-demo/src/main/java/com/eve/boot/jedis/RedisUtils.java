package com.eve.boot.jedis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 类说明
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/10/31
 */
@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, String> template;

    public <T> T get(String key, Class<T> valueType) {
        String json = template.opsForValue().get(key);
        if (valueType.isAssignableFrom(String.class)) {
            return (T) json;
        }
        return JSON.parseObject(json, valueType);
    }

    public <V> void set(String key, V value) {
        if (value instanceof String) {
            template.opsForValue().set(key, (String) value);
            return;
        }
        String json = JSON.toJSONString(value);
        template.opsForValue().set(key, json);
    }

    public <T> List<T> getList(String key, Class<T> t) {
        String json = template.opsForValue().get(key);
        return JSON.parseArray(json, t);
    }


}
