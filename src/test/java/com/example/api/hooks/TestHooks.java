package com.example.api.hooks;

import com.example.api.config.TestConfig;
import io.cucumber.java.Before;

public class TestHooks {

    @Before(order = 0)
    public void logEnvironment() {
        System.out.printf("Running API tests against environment '%s' with base URL '%s'%n",
                TestConfig.environment(),
                TestConfig.baseUrl());
    }
}
