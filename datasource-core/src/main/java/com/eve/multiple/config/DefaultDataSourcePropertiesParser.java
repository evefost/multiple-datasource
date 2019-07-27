
package com.eve.multiple.config;

import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author xieyang
 */
public  class DefaultDataSourcePropertiesParser extends DataSourceBasePropertiesParser<DataSourceProperties> {

    public DefaultDataSourcePropertiesParser(ConfigurableEnvironment environment) {
        super(environment);
    }

    @Override
    protected Class<DataSourceProperties> getPropertiesClass() {
        return DataSourceProperties.class;
    }

    @Override
    protected void processCustomerProperties(DataSourceProperties dataSourceProperties, String propertyKey, String propertyValue) {

    }


}
