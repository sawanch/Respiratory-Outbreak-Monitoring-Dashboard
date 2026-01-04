package com.covidtracker.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConditionalOnProperty(name = "spring.cache.redis.enabled", havingValue = "true", matchIfMissing = false)
@EnableCaching
public class CovidTrackerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CovidTrackerApiApplication.class, args);
    }
}

