
package com.eve.multiple.event;

import org.apache.ibatis.session.Configuration;
import org.springframework.context.ApplicationEvent;

/**
 *  mybatis扫描statement完成事件
 * <p>
 *
 * @author xieyang
 * @date 2019/7/29
 */
public class StatementBuildFinishEvent extends ApplicationEvent {


    private transient Configuration configuration;

    public StatementBuildFinishEvent(Configuration configuration) {
        super(configuration);
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
