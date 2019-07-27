
package com.eve.multiple.annotation;

import java.lang.annotation.*;

/**
 *
 * 用于强制路由到某数
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Master {

}
