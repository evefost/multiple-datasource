package com.eve.boot;

import com.eve.boot.config.ActuallyDatasourceResolver;
import com.eve.multiple.DataSourceResolver;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = {"com.eve"})
@MapperScan(basePackages = {"com.eve.common.dao"})
public class MyBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBootApplication.class);

    }

    @Bean
    DataSourceResolver dataSourceResolver() {
        return new ActuallyDatasourceResolver();
    }

}