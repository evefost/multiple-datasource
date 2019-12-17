package com.eve.common.service.impl;


import com.eve.common.dao.AMapper;
import com.eve.common.entity.User;
import com.eve.common.service.AService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2018/3/2.
 */
@Service
public class AServiceImpl implements AService {

    @Autowired
    private AMapper aMapper;

    @Override
    public Integer save(User user) {
       return aMapper.insertUser(user);
    }

    @Override
    public User queryById(Integer id) {
        User user = aMapper.queryById(id);
        return user;
    }


}
