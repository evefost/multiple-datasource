package com.eve.boot;


import com.eve.boot.jedis.RedisUtils;
import com.eve.common.entity.User;
import com.eve.common.service.AService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController implements ApplicationContextAware{

  public  final Logger logger = LoggerFactory.getLogger(getClass());


  ApplicationContext applicationContext;

  @Autowired
  private AService aService;

  @Autowired
  private RedisUtils redisUtils;


  @GetMapping("getById")
  @ResponseBody
  public User getUser(Integer id) throws Exception {
    return aService.queryById(id);
  }

  @GetMapping("add")
  @ResponseBody
  public Integer getUser2(String name, int age) throws Exception {
    User user = new User();
    user.setName(name);
    user.setAge(age);
    return aService.save(user);
  }

  @GetMapping("redisSave")
  @ResponseBody
  public String getUser2(String key, String value) throws Exception {
    redisUtils.set(key, value);
    return redisUtils.get(key, String.class);
  }

  @GetMapping("redisGet")
  @ResponseBody
  public String getUser2(String key) throws Exception {
    return redisUtils.get(key, String.class);
  }


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
