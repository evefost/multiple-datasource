package com.eve.boot.mq.client.annotation;

import java.lang.annotation.*;

/**
 * 生产者
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/10/12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Producer {

    String containerFactory() default "";

}
