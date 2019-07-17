package com.eve.common.dao;


import com.eve.common.entity.User;
import com.eve.multiple.annotation.Database;

import java.util.List;

/**
 *
 * @author xieyang
 * @date 18/3/3
 */
@Database("ds2")
public interface CMapper {

    void insertUser(User user);

    User getUser(Integer id);

    List<User> listUser();

    User queryById(Integer id);
}
