package com.eve.boot.config;


import com.eve.boot.config.tenant.TenantDatasourcePropertiesLoader;
import com.eve.multiple.DataSourceResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 类说明
 * <p>
 *
 * @date 2019/7/29
 */
@Configuration
public class SimpleConfig {

    @Bean
    DataSourceResolver dataSourceResolver() {
        return new ActuallyDatasourceResolver();
    }


//    @ConfigurationProperties(prefix = "customer_db")
//    @DatabaseProperties(databaseId = "ds7",isDefault = true)
//    HikariProperties testProperties() {
//        return new HikariProperties();
//    }
//
//
//    @ConfigurationProperties(prefix = "customer_db2")
//    @DatabaseProperties(databaseId = "ds8", masterId = "ds7")
//    HikariProperties testProperties2() {
//        return new HikariProperties();
//    }
//
//
//    @ConfigurationProperties(prefix = "customer_db3")
//    @DatabaseProperties(databaseId = "ds9", masterId = "ds7")
//    HikariProperties testProperties3() {
//        return new HikariProperties();
//    }


    @Bean
    TenantDatasourcePropertiesLoader tenantDatasourcePropertiesLoader(){
       return new TenantDatasourcePropertiesLoader();
    }


}
