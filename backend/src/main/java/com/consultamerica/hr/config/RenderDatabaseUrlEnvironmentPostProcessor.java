package com.consultamerica.hr.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class RenderDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String datasourceUrl = environment.getProperty("SPRING_DATASOURCE_URL");
        if (!StringUtils.hasText(datasourceUrl)) {
            return;
        }

        String normalizedUrl = normalizeJdbcUrl(datasourceUrl);
        if (!datasourceUrl.equals(normalizedUrl)) {
            Map<String, Object> props = new HashMap<>();
            props.put("SPRING_DATASOURCE_URL", normalizedUrl);
            environment.getPropertySources().addFirst(new MapPropertySource("renderDatabaseUrl", props));
        }
    }

    static String normalizeJdbcUrl(String datasourceUrl) {
        if (!StringUtils.hasText(datasourceUrl)) {
            return datasourceUrl;
        }

        if (datasourceUrl.startsWith("jdbc:")) {
            return datasourceUrl;
        }

        if (datasourceUrl.startsWith("postgres://") || datasourceUrl.startsWith("postgresql://")) {
            String withoutScheme = datasourceUrl.substring(datasourceUrl.indexOf("://") + 3);
            String[] hostAndPath = withoutScheme.split("@", 2);
            String authAndHost = hostAndPath.length > 1 ? hostAndPath[1] : hostAndPath[0];
            String[] hostAndDb = authAndHost.split("/", 2);
            String hostPort = hostAndDb[0];
            String database = hostAndDb.length > 1 ? hostAndDb[1] : "";
            return "jdbc:postgresql://" + hostPort + (database.isEmpty() ? "" : "/" + database);
        }

        return datasourceUrl;
    }
}
