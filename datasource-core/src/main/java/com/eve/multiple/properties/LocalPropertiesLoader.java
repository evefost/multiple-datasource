
package com.eve.multiple.properties;

/**
 * 类说明
 * <p>
 *
 * @author xieyang
 * @date 2019/7/26
 */
public class LocalPropertiesLoader implements DatasourcePropertiesLoader<BaseDataSourceProperties> {

    private PropertiesParser<BaseDataSourceProperties> propertiesParser;

    public LocalPropertiesLoader(PropertiesParser<BaseDataSourceProperties> propertiesParser) {
        this.propertiesParser = propertiesParser;
    }

    @Override
    public MultipleSourceProperties<BaseDataSourceProperties> loadProperties() {
        MultipleSourceProperties<BaseDataSourceProperties> parse = propertiesParser.parse();
        return parse;
    }
}
