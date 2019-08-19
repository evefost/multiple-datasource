
package com.eve.multiple.annotation;

import java.lang.annotation.*;

/**
 * 如果自定义的数据源url属性名不是url,标识为数据源url属性
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface JdbcUrl {

}
