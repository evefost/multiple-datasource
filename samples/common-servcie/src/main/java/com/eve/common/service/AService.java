package com.eve.common.service;


import com.eve.common.entity.User;

/**
 * Created by Administrator on 2018/3/2.
 */


public interface AService {


  User queryById(Integer id);


  Integer save(User user);

}
