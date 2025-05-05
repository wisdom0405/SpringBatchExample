package com.example.SpringBatchExample.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MetaDBConfig {

    @Primary // DB 2개 이상 등록 시 충돌이 발생하지 않도록 Primary 설정
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource-meta") // application.properties에 있는 변수설정 값을 자동으로 읽어주는 어노테이션
    public DataSource metaDBSource() { // DB 연결소스

        return DataSourceBuilder.create().build();
    }

    @Primary // 충돌 방지
    @Bean
    public PlatformTransactionManager metaTransactionManager() {

        return new DataSourceTransactionManager(metaDBSource());
    }
}
