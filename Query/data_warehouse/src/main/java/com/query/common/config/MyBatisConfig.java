package com.query.common.config;

import org.apache.ibatis.plugin.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类：注册拦截器
 */
@Configuration
public class MyBatisConfig {

    @Autowired
    private SqlExecutionTimeInterceptor sqlExecutionTimeInterceptor;

    /**
     * 注册MyBatis拦截器
     */
    @Bean
    public Interceptor[] mybatisInterceptors() {
        return new Interceptor[]{sqlExecutionTimeInterceptor};
    }
}

