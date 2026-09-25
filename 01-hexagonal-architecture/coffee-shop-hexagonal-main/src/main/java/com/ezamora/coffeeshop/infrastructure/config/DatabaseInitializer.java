package com.ezamora.coffeeshop.infrastructure.config;

import java.sql.SQLException;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Carga los datos de ejemplo de {@code db/coffee_shop.sql} (dialecto MySQL) si la tabla 'orders'
 * no existe o está vacía. Solo en el perfil {@code dev}. Si el seed falla, el arranque falla.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private static final String SEED_SCRIPT = "db/coffee_shop.sql";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        if (ordersTableHasData()) {
            log.info("La base de datos ya contiene datos. Se omite el script de inicialización.");
            return;
        }
        runSeedScript();
    }

    /** Una tabla inexistente cuenta como vacía; cualquier otro error de acceso a datos se propaga. */
    private boolean ordersTableHasData() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
            return count != null && count > 0;
        } catch (BadSqlGrammarException e) {
            log.info("Tabla 'orders' no encontrada. Se ejecutará el script de inicialización.");
            return false;
        }
    }

    private void runSeedScript() {
        try (var connection = jdbcTemplate.getDataSource().getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(SEED_SCRIPT));
            log.info("Script de inicialización ejecutado con éxito.");
        } catch (SQLException | ScriptException e) {
            log.error("Error al ejecutar el script SQL de inicialización", e);
            throw new IllegalStateException("Database seed failed: " + SEED_SCRIPT, e);
        }
    }
}
