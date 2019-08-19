package com.eve.multiple;

/**
 * 方法数据源mapping
 * <p>
 *
 * @author xieyang
 * @date 2019/7/26
 */
public class DatabaseMeta {

    private String methodName;

    /**
     * 数据源id
     */
    private String databaseId;


    private SourceType sourceType;


    String getMethodName() {
        return methodName;
    }

    void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public String toString() {
        return "Method bind Info{" +
                methodName +
                ",databaseId='" + databaseId + '\'' +
                ", sourceType=" + sourceType +
                '}';
    }
}
