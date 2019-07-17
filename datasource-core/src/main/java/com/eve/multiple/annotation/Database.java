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
    String value();
}
