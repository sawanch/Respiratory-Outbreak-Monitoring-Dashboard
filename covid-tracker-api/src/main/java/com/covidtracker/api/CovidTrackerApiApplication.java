package com.covidtracker.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CovidTrackerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CovidTrackerApiApplication.class, args);
    }
}

