package org.example.stationery_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StationeryShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(StationeryShopApplication.class, args);
    }

}
