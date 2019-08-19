
package com.eve.multiple.event;

import org.springframework.context.ApplicationEvent;

/**
 * 数据配置变化事件
 * <p>
 *
 * @author xieyang
 * @date 2019/7/29
 */
public class DatasourceConfigChangeEvent extends ApplicationEvent {


    public DatasourceConfigChangeEvent(Object source) {
        super(source);
    }
}
