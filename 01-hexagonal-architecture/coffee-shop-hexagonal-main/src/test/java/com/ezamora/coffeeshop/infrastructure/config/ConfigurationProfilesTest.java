package com.ezamora.coffeeshop.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

/** El perfil base es seguro (validate, sin SQL en logs); solo `dev` relaja el esquema. */
class ConfigurationProfilesTest {

    private static PropertySource<?> load(String file) throws IOException {
        return new YamlPropertySourceLoader().load(file, new ClassPathResource(file)).get(0);
    }

    @Test
    void baseProfileValidatesTheSchemaAndDoesNotLogSql() throws IOException {
        var base = load("application.yml");

        assertThat(base.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(base.getProperty("spring.jpa.show-sql")).isEqualTo(false);
    }

    @Test
    void devProfileUpdatesTheSchemaAndShowsSql() throws IOException {
        var dev = load("application-dev.yml");

        assertThat(dev.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("update");
        assertThat(dev.getProperty("spring.jpa.show-sql")).isEqualTo(true);
    }
}
