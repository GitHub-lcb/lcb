package com.lcb.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.lcb")
@SpringBootApplication(scanBasePackages = "com.lcb")
public class LcbApplication {
    public static void main(String[] args) {
        SpringApplication.run(LcbApplication.class, args);
    }
}
