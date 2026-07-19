package com.consultamerica.hr.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class RenderDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String datasourceUrl = environment.getProperty("SPRING_DATASOURCE_URL");
        String existingUsername = environment.getProperty("SPRING_DATASOURCE_USERNAME");
        String existingPassword = environment.getProperty("SPRING_DATASOURCE_PASSWORD");
        if (!StringUtils.hasText(datasourceUrl)) {
            datasourceUrl = environment.getProperty("DATABASE_URL");
        }
        if (!StringUtils.hasText(datasourceUrl)) {
            return;
        }

        String normalizedUrl = normalizeJdbcUrl(datasourceUrl);

        Map<String, Object> props = new HashMap<>();
        boolean addedAuth = false;

        // If the URL contains auth (user:pass@host...), extract and set username/password when not already provided
        if (datasourceUrl != null && (datasourceUrl.startsWith("postgres://") || datasourceUrl.startsWith("postgresql://")) && (!StringUtils.hasText(existingUsername) || !StringUtils.hasText(existingPassword))) {
            String withoutScheme = datasourceUrl.substring(datasourceUrl.indexOf("://") + 3);
            String[] parts = withoutScheme.split("@", 2);
            if (parts.length > 1) {
                String authPart = parts[0];
                int idx = authPart.indexOf(':');
                if (idx > 0) {
                    String user = URLDecoder.decode(authPart.substring(0, idx), StandardCharsets.UTF_8);
                    String pass = URLDecoder.decode(authPart.substring(idx + 1), StandardCharsets.UTF_8);
                    if (!StringUtils.hasText(existingUsername)) {
                        props.put("SPRING_DATASOURCE_USERNAME", user);
                        addedAuth = true;
                    }
                    if (!StringUtils.hasText(existingPassword)) {
                        props.put("SPRING_DATASOURCE_PASSWORD", pass);
                        addedAuth = true;
                    }
                }
            }
        }

        boolean urlChanged = !java.util.Objects.equals(datasourceUrl, normalizedUrl);
        if (urlChanged) {
            props.put("SPRING_DATASOURCE_URL", normalizedUrl);
        }

        if (urlChanged || addedAuth) {
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
