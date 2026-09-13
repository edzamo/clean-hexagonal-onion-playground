package com.example.hexagonal.architecture.coffeeshop.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Coffee Shop — Order API")
                        .description("Arquitectura hexagonal de aprendizaje: pedidos de café (Spring WebFlux).")
                        .version("v1"));
    }
}
