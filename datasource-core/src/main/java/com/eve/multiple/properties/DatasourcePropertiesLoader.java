
package com.eve.multiple.properties;

/**
 * 数据源数据加载器接口
 * <p>
 *
 * @author xieyang
 * @date 2019/7/26
 */
public interface DatasourcePropertiesLoader<DSP extends BaseDataSourceProperties> {


    /**
     * 据实现加载数据源属性
     *
     * @return
     */
    SourceProperties<SourceProperties, DSP> loadProperties();

}
