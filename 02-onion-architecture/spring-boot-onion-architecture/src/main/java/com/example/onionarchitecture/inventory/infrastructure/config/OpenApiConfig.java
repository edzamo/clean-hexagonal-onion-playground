package com.example.onionarchitecture.inventory.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory — Onion Architecture API")
                        .description("Proyecto de aprendizaje: Onion Architecture (Jeffrey Palermo) con Spring MVC + Virtual Threads + JPA.")
                        .version("v1"));
    }
}
