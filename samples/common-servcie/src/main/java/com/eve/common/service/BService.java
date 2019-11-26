package com.eve.common.service;


import com.eve.common.entity.User;

import java.util.List;

/**
 * Created by Administrator on 2018/3/2.
 */
//@Database("ds1")
public interface BService {


    //    @Transactional
    void save(User user);

    User queryById(Integer id);

    List<User> getUsers();
}
