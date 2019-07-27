package com.eve.multiple;

/**
 * 方法数据源mapping
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/7/26
 */
public class DatabaseMeta {

    /**
     * 数据源id
     */
    private String databaseId;

    /**
     * 是否为共享源
     */
    private boolean share = false;


    public boolean isShare() {
        return share;
    }

    public void setShare(boolean share) {
        this.share = share;
    }


    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public String toString() {
        return "DatabaseMeta{" +
                "databaseId='" + databaseId + '\'' +
                ", share=" + share +
                '}';
    }
}
