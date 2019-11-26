package com.eve.common.service.impl;


import com.eve.common.dao.BMapper;
import com.eve.common.entity.User;
import com.eve.common.service.BService;
import com.eve.multiple.SourceType;
import com.eve.multiple.annotation.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Administrator on 2018/3/2.
 */

@Service
@Database(value = "ds0",type = SourceType.TENANT)
public class BServiceImpl implements BService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private BMapper bMapper;


    @Transactional
    @Override
    public void save(User user) {
        bMapper.insertUser(user);

    }

//    @Transactional
    @Override
    public User queryById(Integer id) {
        return bMapper.queryById(id);
    }


    @Override
    public List<User> getUsers() {
        return bMapper.listUser();
    }
}
