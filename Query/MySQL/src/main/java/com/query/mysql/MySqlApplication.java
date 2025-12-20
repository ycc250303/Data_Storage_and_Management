package com.query.mysql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MySqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySqlApplication.class, args);
    }

}
