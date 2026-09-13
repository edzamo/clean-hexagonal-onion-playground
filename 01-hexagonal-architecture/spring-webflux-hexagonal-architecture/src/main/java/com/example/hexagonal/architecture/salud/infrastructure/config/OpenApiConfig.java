package com.example.hexagonal.architecture.salud.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI appointmentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Salud — Appointment API")
                        .description("Arquitectura hexagonal de aprendizaje: gestión de citas médicas (Spring WebFlux).")
                        .version("v1"));
    }
}
