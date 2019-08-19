package com.eve.common.dao;


import com.eve.common.entity.User;

import java.util.List;

/**
 * Created by xieyang on 18/3/3.
 */
//@Database("ds0_1")
public interface AMapper {

    void insertUser(User user);

    User getUser(Integer id);

    List<User> listUser();

    User queryById(Integer id);
}
