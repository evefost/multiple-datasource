package com.eve.boot;


import com.eve.common.dao.AMapper;
import com.eve.common.entity.User;
import com.eve.common.service.AService;
import com.xxx.starter.DatasourceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class TestController implements ApplicationContextAware{

  public  final Logger logger = LoggerFactory.getLogger(getClass());



  ApplicationContext applicationContext;

  @Autowired
  private Environment environment;

  @Autowired
  private AService aService;

  @Autowired
  private DatasourceProperties datasourceProperties;


  @GetMapping("getUser")
  @ResponseBody
  public User getUser(String name) throws Exception {
    return aService.queryById(7);
  }
  @Autowired
  private AMapper aMapper;
  @GetMapping("getUser2")
  @ResponseBody
  public User getUser2(String name) throws Exception {
    return aMapper.queryById(7);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
