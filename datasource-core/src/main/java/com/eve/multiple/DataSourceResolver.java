
package com.eve.multiple;



import com.eve.multiple.properties.BaseDataSourceProperties;

import javax.sql.DataSource;

/**
 * @author Administrator
 */
public interface DataSourceResolver<T extends DataSource, DP extends BaseDataSourceProperties> {

    T createDataSource(DP properties);
}
