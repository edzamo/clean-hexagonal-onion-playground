package com.example.cleanarchitecture.bank.frameworks.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank — Clean Architecture API")
                        .description("Proyecto de aprendizaje: Clean Architecture (Uncle Bob) con Spring MVC + Virtual Threads + JPA.")
                        .version("v1"));
    }
}
