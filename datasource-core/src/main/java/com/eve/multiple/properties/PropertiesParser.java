
package com.eve.multiple.properties;

/**
 * @author xieyang
 */
public interface PropertiesParser<DP extends BaseDataSourceProperties> {


    /**
     * 解释属性
     *
     * @return
     */
    MultipleSourceProperties<DP> parse();

}
