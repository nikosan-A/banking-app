package com.example.banking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/bankdb")
                .username("bankuser")
                .password("securepassword")
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}