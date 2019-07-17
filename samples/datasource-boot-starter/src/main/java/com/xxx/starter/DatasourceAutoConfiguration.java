package com.xxx.starter;

import com.eve.multiple.DataSourceResolver;
import com.eve.multiple.config.DatasourceConfig;
import com.eve.multiple.datasource.MultipleDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 */
@Configuration
@EnableConfigurationProperties({DatasourceProperties.class})
public class DatasourceAutoConfiguration {

    @Bean
    DatasourceConfig getDatasourceConfig(DataSourceResolver resolver) {
        DatasourceConfig datasourceConfig = new DatasourceConfig();
        datasourceConfig.setDataSourceResolver(resolver);
        return datasourceConfig;
    }

    @Bean
    MultipleDataSource getMultipleDataSource() {
        return new MultipleDataSource();
    }


}
