package com.example.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {
    private static final String DEFAULT_ENV = "local";
    private static final Properties PROPERTIES = loadProperties();

    private TestConfig() {
    }

    public static String environment() {
        return System.getProperty("test.env", DEFAULT_ENV);
    }

    public static String baseUrl() {
        return get("base.url");
    }

    public static int requestTimeoutMs() {
        return Integer.parseInt(get("request.timeout.ms", "10000"));
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        return PROPERTIES.getProperty(key, defaultValue);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        String env = environment();
        String path = "config/env/" + env + ".properties";

        try (InputStream input = TestConfig.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("Environment config not found: " + path);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load environment config: " + path, e);
        }

        return properties;
    }
}
