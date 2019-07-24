package test.com.eve.datasource.extend;

import com.eve.multiple.config.DataSourceProperties;

/**
 * Created by xieyang on 19/7/25.
 */
public class CustomerProperties  extends DataSourceProperties {

    private String timeout;

    private String merchantId;

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}
