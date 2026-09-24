package com.ezamora.coffeeshop.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

class DatabaseInitializerTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final DataSource dataSource = mock(DataSource.class);
    private final DatabaseInitializer initializer = new DatabaseInitializer(jdbcTemplate);

    @Test
    void onlyRunsInTheDevProfile() {
        assertThat(DatabaseInitializer.class.getAnnotation(Profile.class).value()).containsExactly("dev");
    }

    @Test
    void skipsTheSeedWhenOrdersAlreadyExist() throws Exception {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class))).thenReturn(2);

        initializer.run();

        verify(jdbcTemplate, never()).getDataSource();
    }

    @Test
    void failsFastWhenTheSeedScriptCannotRun() throws Exception {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class)))
                .thenThrow(new BadSqlGrammarException("count", "SELECT", new SQLException("no table")));
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));

        assertThatThrownBy(initializer::run).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seed");
    }

    @Test
    void unexpectedDataAccessErrorWhileCountingIsNotSwallowedAsMissingTables() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class)))
                .thenThrow(new DataAccessResourceFailureException("connection refused"));

        assertThatThrownBy(initializer::run).isInstanceOf(DataAccessResourceFailureException.class);
    }
}
