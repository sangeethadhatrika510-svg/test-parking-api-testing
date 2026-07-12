package com.example.api.utils;

import com.example.api.config.TestConfig;

public class BaseTest {
    public static String apiUrl() {
        return TestConfig.serviceBaseUrl("api");
    }

    public static String authUrl() {
        return TestConfig.serviceBaseUrl("auth");
    }

    public static String locationUrl() {
        return TestConfig.serviceBaseUrl("location");
    }

    private BaseTest() {
    }
}

