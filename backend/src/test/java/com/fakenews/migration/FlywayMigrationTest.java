package com.fakenews.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Flyway Database Migrations Test")
class FlywayMigrationTest {

    @Test
    @DisplayName("Should successfully apply V1 and V2 migrations and create users and analyses tables")
    void testFlywayMigrationsApplyCleanly() throws Exception {
        DataSource dataSource = DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:flyway_test_db;DB_CLOSE_DELAY=-1;MODE=MySQL")
                .username("sa")
                .password("")
                .build();

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        int applied = flyway.migrate().migrationsExecuted;
        System.out.println("Flyway successfully executed migrations count: " + applied);

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            Set<String> tables = new HashSet<>();
            try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }

            System.out.println("Created tables in database: " + tables);
            assertTrue(tables.contains("users"), "Table 'users' should have been created by V1");
            assertTrue(tables.contains("analyses"), "Table 'analyses' should have been created by V2");
            assertTrue(tables.contains("flyway_schema_history"), "Table 'flyway_schema_history' should exist");
        }
    }
}
