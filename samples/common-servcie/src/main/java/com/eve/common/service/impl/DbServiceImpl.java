package com.eve.common.service.impl;

import com.eve.common.dao.AMapper;
import com.eve.common.service.DbService;
import com.eve.multiple.SourceType;
import com.eve.multiple.annotation.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xieyang on 19/7/17.
 */
@Service
@Database(value = "ds0",type = SourceType.TENANT)
public class DbServiceImpl implements DbService {
    @Autowired
    private AMapper aMapper;


}
