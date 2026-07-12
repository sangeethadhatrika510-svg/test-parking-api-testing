package com.example.api.hooks;

import com.example.api.config.TestConfig;
import com.example.api.context.TestScenarioContext;
import io.cucumber.java.Before;

public class TestHooks {

    @Before(order = 0)
    public void logEnvironment() {
        TestScenarioContext.reset();
        System.out.printf("Running API tests against environment '%s'%n", TestConfig.environment());
        System.out.printf("Auth URL: %s | Location URL: %s%n",
                TestConfig.serviceBaseUrl("auth"), TestConfig.serviceBaseUrl("location"));
    }
}
