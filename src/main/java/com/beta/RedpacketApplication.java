package com.beta;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {"com.beta.mapper"})
public class RedpacketApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedpacketApplication.class, args);
    }

}

