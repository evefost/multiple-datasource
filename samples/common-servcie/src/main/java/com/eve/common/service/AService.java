package com.eve.common.service;


import com.eve.common.entity.User;
import com.eve.multiple.annotation.Database;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Administrator on 2018/3/2.
 */
@Database("ds0")

public interface AService {

// @Transactional
  User queryById(Integer id);

  User queryByIdWithTransaction(Integer id);

//  @Transactional
  void save(User user);

  void saveWithTransaction(User user);

  List<User> getUsers();




  void queryByIdMutipleDao(Integer id);


  @Transactional
  void saveMutipleDao(User user);


//  @Transactional
  void mutipleOperate();


//    @Transactional
  void mutipleOperate2();
}
