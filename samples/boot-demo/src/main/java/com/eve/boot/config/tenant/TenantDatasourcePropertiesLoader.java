package com.eve.boot.config.tenant;



import com.eve.boot.config.HikariProperties;
import com.eve.multiple.properties.CompositeMultipleSourceProperties;
import com.eve.multiple.properties.DatasourcePropertiesLoader;
import com.eve.multiple.properties.MultipleSourceProperties;
import com.eve.multiple.properties.SourceProperties;

import java.util.HashMap;
import java.util.Map;


/**
 * 租户数据源属性加载器
 * <p>
 *
 * @author 谢洋
 * @version 1.0.0
 * @date 2019/7/26
 */
public class TenantDatasourcePropertiesLoader implements DatasourcePropertiesLoader<HikariProperties> {


    @Override
    public SourceProperties<SourceProperties, HikariProperties> loadProperties() {
        return mockData();
    }

    private CompositeMultipleSourceProperties<HikariProperties> mockData() {

        CompositeMultipleSourceProperties<HikariProperties> compositeMultipleSourceProperties = new CompositeMultipleSourceProperties<HikariProperties>();
        HikariProperties properties1 = new HikariProperties();
        properties1.setUrl("jdbc:mysql://localhost:3306/ds0?useUnicode=true&characterEncoding=utf-8&useSSL=false");
        properties1.setPassword("root");
        properties1.setUsername("root");
        properties1.setTenantId("1");
        properties1.setId("ds0");
        properties1.setDefault(true);



        Map<String, HikariProperties> tenantMap = new HashMap<String, HikariProperties>();
        tenantMap.put(properties1.getId(), properties1);
        MultipleSourceProperties<HikariProperties> multipleSourceProperties = new MultipleSourceProperties();
        multipleSourceProperties.setSource(properties1.getTenantId());
        multipleSourceProperties.setDefaultDatabaseId(properties1.getId());
        multipleSourceProperties.setDatasourceProperties(tenantMap);
        compositeMultipleSourceProperties.add(multipleSourceProperties);

        HikariProperties properties2 = new HikariProperties();
        properties2.setUrl("jdbc:mysql://localhost:3306/ds1?useUnicode=true&characterEncoding=utf-8&useSSL=false");
        properties2.setPassword("root");
        properties2.setUsername("root");
        properties2.setTenantId("2");
        properties2.setId("ds0");


        Map<String, HikariProperties> tenantMap2 = new HashMap<String, HikariProperties>();
        tenantMap2.put(properties2.getId(), properties2);
        MultipleSourceProperties<HikariProperties> multipleSourceProperties2 = new MultipleSourceProperties();
        multipleSourceProperties2.setSource(properties2.getTenantId());
        multipleSourceProperties2.setDefaultDatabaseId(properties2.getId());
        multipleSourceProperties2.setDatasourceProperties(tenantMap2);

        compositeMultipleSourceProperties.add(multipleSourceProperties2);
        return compositeMultipleSourceProperties;
    }
}
