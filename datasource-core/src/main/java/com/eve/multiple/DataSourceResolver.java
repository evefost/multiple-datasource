
package com.eve.multiple;



import com.eve.multiple.config.DataSourceProperties;

import javax.sql.DataSource;

/**
 * @author Administrator
 */
public interface DataSourceResolver<T extends DataSource,DP extends DataSourceProperties> {

    T createDataSource(DP properties);
}
