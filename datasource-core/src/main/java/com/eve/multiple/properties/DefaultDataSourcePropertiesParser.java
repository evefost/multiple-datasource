
package com.eve.multiple.properties;

import com.eve.multiple.DataSourceResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.lang.reflect.Method;

/**
 * 数据源属性默认解释器
 * @author xieyang
 */
public class DefaultDataSourcePropertiesParser extends AbstractDataSourceBasePropertiesParser<BaseDataSourceProperties> {

    private BeanFactory beanFactory;

    public DefaultDataSourcePropertiesParser(ConfigurableEnvironment environment, BeanFactory beanFactory) {
        super(environment);
        this.beanFactory = beanFactory;
    }

    @Override
    protected Class<BaseDataSourceProperties> getPropertiesClass() {
        //从 DataSourceResolver 用户实现中共聚 Properties的实际类型,
        DataSourceResolver bean = beanFactory.getBean(DataSourceResolver.class);
        try {
            Method[] declaredMethods = bean.getClass().getDeclaredMethods();
            for (Method md : declaredMethods) {
                Class<?>[] parameterTypes = md.getParameterTypes();
                if (parameterTypes.length == 1 && BaseDataSourceProperties.class.isAssignableFrom(parameterTypes[0])) {
                    return (Class<BaseDataSourceProperties>) parameterTypes[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BaseDataSourceProperties.class;
    }

    @Override
    protected void processCustomerProperties(BaseDataSourceProperties dataSourceProperties, String propertyKey, String propertyValue) {

    }


}
