package com.xxx.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 配置了才启用
 *
 * @author xieyang
 */

@ConfigurationProperties("datasource")
public class DatasourceProperties {

    private Map<String,String> master;

    private Map<String,String> slaver;

    public Map<String, String> getSlaver() {
        return slaver;
    }

    public void setSlaver(Map<String, String> slaver) {
        this.slaver = slaver;
    }

    public Map<String, String> getMaster() {
        return master;
    }

    public void setMaster(Map<String, String> master) {
        this.master = master;
    }
}
