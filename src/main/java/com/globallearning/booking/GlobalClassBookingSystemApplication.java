package com.globallearning.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GlobalClassBookingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlobalClassBookingSystemApplication.class, args);
    }

}

