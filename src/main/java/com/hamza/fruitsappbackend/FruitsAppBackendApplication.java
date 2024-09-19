package com.hamza.fruitsappbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class FruitsAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitsAppBackendApplication.class, args);
    }

}
