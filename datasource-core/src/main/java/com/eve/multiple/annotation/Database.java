
package com.eve.multiple.annotation;

import java.lang.annotation.*;

/**
 *
 * 据数据源id指定数据源
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Database {

    /**
     * 数据源id
     * @return
     */
    String value();

    /**
     * 是否为共同，默认为true,如果是多租户应设为false,表示该数据源不共享,属于某个租户的数据源
     * @return
     */
    boolean share() default true;

}
