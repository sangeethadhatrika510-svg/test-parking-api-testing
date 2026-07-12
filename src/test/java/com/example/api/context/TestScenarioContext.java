package com.example.api.context;

import io.restassured.response.Response;

public final class TestScenarioContext {
    private static final ThreadLocal<TestScenarioContext> CURRENT =
            ThreadLocal.withInitial(TestScenarioContext::new);

    public String username;
    public String email;
    public String password;
    public String token;
    public String zoneCode;
    public String locationName;
    public String spaceCode;
    public int zoneId;
    public int locationId;
    public Response response;

    private TestScenarioContext() {
    }

    public static TestScenarioContext current() {
        return CURRENT.get();
    }

    public static void reset() {
        CURRENT.set(new TestScenarioContext());
    }
}
