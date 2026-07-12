package com.example.api.utils;

import com.example.api.api.LocationAPI;
import com.example.api.api.LoginAPI;
import com.example.api.api.RegisterAPI;
import com.example.api.api.ZonesAPI;
import com.example.api.config.TestConfig;
import com.example.api.context.TestScenarioContext;
import io.restassured.response.Response;
import java.util.List;

public final class TestFixtures {
    private TestFixtures() {
    }

    public static void createAdmin(TestScenarioContext context) {
        String suffix = Long.toString(System.nanoTime(), 36);
        context.username = "qa_" + suffix;
        context.email = context.username + "@example.test";
        context.password = TestConfig.get("test.password", "Password123!");

        Response response = RegisterAPI.register(
                context.username, context.email, context.password, List.of("ADMIN", "USER"));
        requireStatus(response, 201, "register test administrator");
    }

    public static void loginAdmin(TestScenarioContext context) {
        if (context.username == null) {
            createAdmin(context);
        }
        Response response = LoginAPI.login(context.username, context.password);
        requireStatus(response, 200, "login test administrator");
        context.token = response.jsonPath().getString("accessToken");
        if (context.token == null || context.token.isBlank()) {
            throw new IllegalStateException("Login succeeded without an accessToken. Body: " + response.asString());
        }
    }

    public static void createZone(TestScenarioContext context) {
        if (context.token == null) {
            loginAdmin(context);
        }
        context.zoneCode = "ZONE-" + Long.toString(System.nanoTime(), 36).toUpperCase();
        Response response = ZonesAPI.createZone(context.token, context.zoneCode,
                "QA Parking Zone", "Dublin", "Created by API automation", true);
        requireStatus(response, 201, "create prerequisite zone");
        context.zoneId = response.jsonPath().getInt("id");
    }

    public static void createLocation(TestScenarioContext context) {
        if (context.zoneId == 0) {
            createZone(context);
        }
        context.locationName = "QA Location " + Long.toString(System.nanoTime(), 36);
        Response response = LocationAPI.createLocation(context.token, context.zoneId,
                context.locationName, "1 Test Street", "UNMAPPED_AREA", true);
        requireStatus(response, 201, "create prerequisite location");
        context.locationId = response.jsonPath().getInt("id");
    }

    public static void requireStatus(Response response, int expected, String operation) {
        if (response.statusCode() != expected) {
            throw new IllegalStateException("Unable to " + operation + ". Expected " + expected
                    + " but received " + response.statusCode() + ". Body: " + response.asString());
        }
    }
}
