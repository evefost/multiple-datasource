package com.eve.multiple.properties;



import com.eve.multiple.DatabaseMeta;
import com.eve.multiple.PropertiesSource;

import java.util.Map;

/**
 * 数据源属性顶层接口
 * <p>
 *
 * @author xieyang
 * @date 2019/7/31
 */
public interface SourceProperties<SP extends SourceProperties, DP extends BaseDataSourceProperties> {


    /**
     * 获取当前属性配置的来源
     * 默认为{@link PropertiesSource#DEFAULT} 指本地配置或匹配中的的数据
     * 其它一般指租户源
     *
     * @return
     */
    default String getSource() {
        return "undefine";
    }

    ;


    /**
     * 设置当前属性配置的来源
     * {@link PropertiesSource}
     *
     * @param source
     */
    default void setSource(String source) {

    }

    /**
     * 返回数据源信息
     *
     * @return
     */
    Map<String, DP> getDatasourceProperties();


    /**
     * 据数据源id,及执行方法元数据获取相应数据源
     * @param databaseId
     * @param meta
     * @return
     */
    DP getProperties(String databaseId, DatabaseMeta meta);


    /**
     * 获取默认数据源id
     * @return
     */
    default String getDefaultDatabaseId() {
        return "undefine";
    }

    /**
     *  设置默认数据源id
     *
     * @param defaultDatabaseId
     */
    default void setDefaultDatabaseId(String defaultDatabaseId) {

    }

    /**
     * 获取默认数据原
     * 共享源或租户源都有可能有默认的数据源
     * @param meta
     * @return
     */
    DP getDefaultDatabaseProperties(DatabaseMeta meta);

    /**
     * 合并数据，如果id 一样的，将被合并成一个
     * @param sourceProperties
     */
    default void merge(SP sourceProperties) {
    }

    /**
     * 获取实际数据源属性内容
     * @return
     */
    default Map<String, SourceProperties<SourceProperties, DP>> getMultipleProperties() {
        return null;
    }
}
