package com.eve.multiple.properties;

/**
 * 扫描自动装配置的数据源属性接口
 *
 * @author xieyang
 * @date 19/8/2
 */
public interface AutoConfigScanner {

    /**
     * 扫描接口
     *
     * @throws ClassNotFoundException ClassNotFoundException
     * @throws IllegalAccessException IllegalAccessException
     * @return SourceProperties 数据源属性集
     */
    SourceProperties<SourceProperties, BaseDataSourceProperties> scanProperties() throws ClassNotFoundException, IllegalAccessException;


}
