package com.eve.common.dao;



import com.eve.common.entity.User;
import com.eve.multiple.annotation.Database;

import java.util.List;

/**
 * Created by xieyang on 18/3/3.
 */
@Database("ds1_1")
public interface BMapper {

    void insertUser(User user);

    User getUser(Integer id);

    List<User> listUser();

    User queryById(Integer id);

}
