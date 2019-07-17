package com.eve.common.service.impl;

import com.eve.common.dao.CMapper;
import com.eve.common.entity.User;
import com.eve.common.service.CService;
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
@Database("ds2")
public class CServiceImpl implements CService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CMapper cMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(User user) {
        cMapper.insertUser(user);
    }

    @Override
    public User queryById(Integer id) {
        return cMapper.queryById(id);
    }


    @Override
    public List<User> getUsers() {
        return cMapper.listUser();
    }
}
