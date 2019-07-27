
package com.eve.multiple;


import com.eve.multiple.config.DataSourceProperties;
import com.eve.multiple.config.MultipleSourceProperties;
import com.eve.multiple.config.PropertiesParser;

/**
 * 类说明
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/7/26
 */
public class LocalPropertiesLoader implements DatasourcePropertiesLoader {

    private PropertiesParser<DataSourceProperties> propertiesParser;

    public  LocalPropertiesLoader(PropertiesParser<DataSourceProperties> propertiesParser) {
        this.propertiesParser = propertiesParser;
    }

    @Override
    public MultipleSourceProperties<DataSourceProperties> loadProperties(){
        MultipleSourceProperties<DataSourceProperties> parse = propertiesParser.parse();
        return parse;
    }
}
