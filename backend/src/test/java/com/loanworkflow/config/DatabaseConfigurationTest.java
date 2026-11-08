package com.loanworkflow.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigurationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoadsAndDataSourceIsConfigured() throws SQLException {
        assertNotNull(dataSource, "DataSource bean must be present in application context");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Database connection should not be null");
            DatabaseMetaData metaData = connection.getMetaData();
            assertNotNull(metaData);
            String productName = metaData.getDatabaseProductName();
            assertTrue(productName.equalsIgnoreCase("H2"), "Database product should be H2 in test profile");
        }
    }
}
