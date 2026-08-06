package com.officewars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OfficeWarsApplication {
    public static void main(String[] args) {
        SpringApplication.run(OfficeWarsApplication.class, args);
    }
}
