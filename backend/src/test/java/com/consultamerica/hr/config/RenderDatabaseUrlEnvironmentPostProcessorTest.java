package com.consultamerica.hr.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderDatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void convertsRenderPostgresConnectionStringToJdbcUrl() {
        assertEquals(
                "jdbc:postgresql://dpg-example.render.com:5432/consult-america-hr-db",
                RenderDatabaseUrlEnvironmentPostProcessor.normalizeJdbcUrl(
                        "postgres://user:pass@dpg-example.render.com:5432/consult-america-hr-db"
                )
        );
    }

    @Test
    void leavesExistingJdbcUrlUnchanged() {
        assertEquals(
                "jdbc:postgresql://localhost:5432/hrdb",
                RenderDatabaseUrlEnvironmentPostProcessor.normalizeJdbcUrl("jdbc:postgresql://localhost:5432/hrdb")
        );
    }
}
