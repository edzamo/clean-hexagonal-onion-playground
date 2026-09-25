package com.ezamora.coffeeshop;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CoffeeShopMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeShopMainApplication.class, args);
    }

    /** Reloj inyectable en los casos de uso (los tests lo fijan). */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
