package com.rocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * @author yzy
 * @create 2022-12-22-22:19
 */
@Configuration
public class BaseConfig {
    @Bean
    public SimpleDateFormat getSimpleDateFormat(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }
}
