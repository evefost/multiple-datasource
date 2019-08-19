package com.eve.boot.config;


import com.eve.multiple.DataSourceResolver;
import com.eve.multiple.annotation.DatabaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
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


    @ConfigurationProperties(prefix = "customer_db")
    @DatabaseProperties(databaseId = "ds7",isDefault = true)
    HikariProperties testProperties() {
        return new HikariProperties();
    }


    @ConfigurationProperties(prefix = "customer_db2")
    @DatabaseProperties(databaseId = "ds8", masterId = "ds7")
    HikariProperties testProperties2() {
        return new HikariProperties();
    }


    @ConfigurationProperties(prefix = "customer_db3")
    @DatabaseProperties(databaseId = "ds9", masterId = "ds7")
    HikariProperties testProperties3() {
        return new HikariProperties();
    }




}
