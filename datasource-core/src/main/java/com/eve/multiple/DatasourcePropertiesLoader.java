
package com.eve.multiple;


import com.eve.multiple.config.DataSourceProperties;
import com.eve.multiple.config.MultipleSourceProperties;

/**
 * 数据源数据加载器
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/7/26
 */
public interface DatasourcePropertiesLoader {


    MultipleSourceProperties<DataSourceProperties> loadProperties() ;

}
