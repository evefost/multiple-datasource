package com.eve.common.dao;


import com.eve.common.entity.User;
import com.eve.multiple.SourceType;
import com.eve.multiple.annotation.Database;

import java.util.List;

/**
 * Created by xieyang on 18/3/3.
 */
@Database(value = "ds0",type = SourceType.TENANT)
public interface AMapper {

    Integer insertUser(User user);

    User queryByName(String name);

    User getUser(Integer id);

    List<User> listUser();

    User queryById(Integer id);
}
