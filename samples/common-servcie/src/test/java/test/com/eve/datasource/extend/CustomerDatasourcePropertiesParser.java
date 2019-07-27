package test.com.eve.datasource.extend;

import com.eve.multiple.config.DataSourceBasePropertiesParser;

/**
 * Created by xieyang on 19/7/25.
 */
public class CustomerDatasourcePropertiesParser extends DataSourceBasePropertiesParser<CustomerProperties> {


    @Override
    protected Class<CustomerProperties> getPropertiesClass() {
        return CustomerProperties.class;
    }

    @Override
    protected void processCustomerProperties(CustomerProperties dataSourceProperties, String propertyKey, String propertyValue) {
        if (match(propertyKey,"timeout")) {
            dataSourceProperties.setTimeout(propertyValue);
        }
        if (match(propertyKey,"merchantId")) {
            dataSourceProperties.setMerchantId(propertyValue);
        }
    }


}
