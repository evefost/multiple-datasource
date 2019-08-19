
package com.eve.multiple.properties;

import java.util.ArrayList;
import java.util.List;

/**
 *  数据源属性基类
 * <p>
 *
 * @author xieyang
 * @date 2019/7/26
 */
public class BaseDataSourceProperties {

    /**
     * 数据源id
     */
    private String id;

    /**
     * 租户id
     */
    private String tenantId;

    private String url;

    private String username;

    private String password;

    /**
     * 如果是从库，有所属主库id
     */
    private String parentId;

    /**
     * 是否为默认库
     */
    private boolean isDefault;
    /**
     * 如果主从可能有从库
     */
    private List<BaseDataSourceProperties> slavers = new ArrayList<>();

    public boolean isMaster() {
        return parentId == null;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    public List<BaseDataSourceProperties> getSlavers() {
        return slavers;
    }


    public void addSlaver(BaseDataSourceProperties properties) {
        this.slavers.add(properties);
    }

    @Override
    public String toString() {
        return "BaseDataSourceProperties{" +
                "id='" + id + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", parentId='" + parentId + '\'' +
                ", isDefault=" + isDefault +
                ", slavers=" + slavers +
                '}';
    }
}
