package com.eve.boot.config;


import com.eve.multiple.properties.BaseDataSourceProperties;

/**
 * jjjjjj
 *
 * @author xieyang
 * @date 19/7/25
 */
public class HikariProperties extends BaseDataSourceProperties {

    private String timeout;


    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

}
