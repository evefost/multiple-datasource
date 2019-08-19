
package com.eve.multiple.annotation;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 *
 * @author xieyang
 * @date 2019/8/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Bean
public @interface DatabaseProperties {


    /**
     * 是否设为默认数据源
     *
     * @return default false
     */
    boolean isDefault() default false;

    /**
     * 设置定数据源id
     *
     * @return default ""
     */
    @AliasFor("databaseId")
    String value() default "";

    /**
     * 设置定数据源id
     * @return default ""
     */
    @AliasFor("value")
    String databaseId() default "";

    /**
     * 指定该属性的主库id
     * 如果没指定，则属性源为主库，否则为指定主库的从库
     *
     * @return default ""
     */
    String masterId() default "";

}
