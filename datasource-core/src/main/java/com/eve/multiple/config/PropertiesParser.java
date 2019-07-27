
package com.eve.multiple.config;

/**
 * @author xieyang
 */
public interface PropertiesParser<DP extends DataSourceProperties> {


    MultipleSourceProperties<DP> parse();

}
