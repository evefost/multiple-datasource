package com.eve.multiple.config;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author xieyang
 */
public class DataSourceProperties {

    private String url;

    private String username;

    private String password;
    /**
     * 数据源id
     */
    private String id;

    /**
     * 如果是从库，有所属主库id
     */
    private String parentId;

    /**
     * 是否为默认库
     */
    private boolean isDefault;

    public boolean isMaster() {
        return parentId == null;
    }


    /**
     *如果主从可能有从库
     */
    private List<DataSourceProperties> slavers = new ArrayList<>();

    public String getUrl() {
        return url;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public List<DataSourceProperties> getSlavers() {
        return slavers;
    }

    public void setSlavers(List<DataSourceProperties> slavers) {
        this.slavers = slavers;
    }


    public void addSlaver(DataSourceProperties properties){
        this.slavers.add(properties);
    }
}
