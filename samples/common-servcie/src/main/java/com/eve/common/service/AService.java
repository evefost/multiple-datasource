package com.eve.common.service;


import com.eve.common.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Administrator on 2018/3/2.
 */


public interface AService {

// @Transactional
  User queryById(Integer id);

  User queryByIdWithTransaction(Integer id);

  Integer save(User user);

  void saveWithTransaction(User user);

  List<User> getUsers();




  void queryByIdMutipleDao(Integer id);


  @Transactional
  void saveMutipleDao(User user);


//  @Transactional
  void mutipleOperate();


//    @Transactional
  void mutipleOperate2();


  User queryByName(String name);
}
