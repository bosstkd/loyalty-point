package com.carrefour.kata.presentation.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie que le schéma de la base est bien créé par le changelog Liquibase
 * (et non par Hibernate, configuré en {@code ddl-auto: validate}).
 */
@SpringBootTest
class LiquibaseSchemaIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void all_changesets_are_applied_at_startup() {
        List<String> appliedChangeSets = jdbc.queryForList(
                "SELECT id FROM databasechangelog ORDER BY orderexecuted", String.class);

        assertThat(appliedChangeSets).containsExactly(
                "001-create-loyalty-account",
                "002-create-points-lot",
                "003-create-voucher");
    }

    @Test
    void schema_contains_all_tables() {
        List<String> tables = jdbc.queryForList(
                "SELECT LOWER(table_name) FROM information_schema.tables WHERE table_schema = 'PUBLIC'",
                String.class);

        assertThat(tables).contains("loyalty_account", "points_lot", "voucher");
    }
}
