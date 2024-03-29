
package com.eve.multiple.annotation;

import com.eve.multiple.SourceType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import static com.eve.multiple.SourceType.SHARE;

/**
 *
 * @author xieyang
 * @date 2019/8/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Database {

    /**
     * 设置定数据源id
     *
     * @return default ""
     */
    @AliasFor("databaseId")
    String value() default "";

    /**
     * 设置定数据源id
     *
     * @return default ""
     */
    @AliasFor("value")
    String databaseId() default "";


    /**
     * 数据源类型
     *
     * @return default {@link SourceType#SHARE}
     */
    SourceType type() default SHARE;

}
