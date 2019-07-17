package com.eve.mvc.datasource.controller;


import com.eve.common.entity.User;
import com.eve.common.service.AService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Administrator on 2018/3/9.
 */
@Controller
public class TestController implements ApplicationContextAware{

  public  final Logger logger = LoggerFactory.getLogger(getClass());


  @Value("${datasource.default.ds-id}")
  private String defaultId;

  ApplicationContext applicationContext;

  @Autowired
  private Environment environment;

  @Autowired
  private AService aService;

  @GetMapping("getUser1")
  @ResponseBody
  public String getUser2(){
    String property = environment.getProperty("datasource.master");
    logger.info("getUser1:"+property);
    return "xieyang";
  }

  @GetMapping("getUser")
  @ResponseBody
  public User getUser(String name) throws Exception {
    return aService.queryById(12);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
