package com.security.learn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.security.learn.mapper")
public class SecurityLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityLearnApplication.class, args);
    }
}
