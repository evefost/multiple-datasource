package com.eve.common.service.impl;


import com.eve.common.dao.AMapper;
import com.eve.common.dao.BMapper;
import com.eve.common.entity.User;
import com.eve.common.service.AService;
import com.eve.common.service.BService;
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
@Database("ds0")
@Service
public class AServiceImpl implements AService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AMapper aMapper;

    @Autowired
    private BMapper bMapper;

    @Autowired
    private BService bService;

    @Autowired
    private CService cService;


    /**
     * @param user
     */
    @Override
    public void save(User user) {
        aMapper.insertUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithTransaction(User user) {
        aMapper.insertUser(user);
    }

    @Transactional
    @Override
    public User queryById(Integer id) {
        User user = aMapper.queryById(id);
        user.setAge(22);
        user.setName("老王");
        save(user);
//        bService.save(user);
        bService.queryById(1);
        cService.queryById(1);
//        cService.save(user);
//        if (true) {
//            throw new RuntimeException("xxxx");
//        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User queryByIdWithTransaction(Integer id) {
        return aMapper.queryById(id);
    }


    @Override
    public List<User> getUsers() {
        return aMapper.listUser();
    }

    @Override
    public void queryByIdMutipleDao(Integer id) {
        aMapper.queryById(id);
        bMapper.queryById(id);
    }

    @Override
    public void saveMutipleDao(User user) {
        aMapper.insertUser(user);
        //bMapper.insertUser(user);
        bService.save(user);
    }

    @Override
    public void mutipleOperate() {
        aMapper.queryById(1);
        User user = new User();
        user.setAge(22);
        user.setName("mutipleOperate");
        aMapper.insertUser(user);
        aMapper.queryById(2);
    }

    @Override
    public void mutipleOperate2() {
        aMapper.queryById(1);
        User user = new User();
        user.setAge(22);
        user.setName("00000000000");
        bMapper.insertUser(user);
        aMapper.queryById(2);
        bMapper.queryById(2);
    }
}
