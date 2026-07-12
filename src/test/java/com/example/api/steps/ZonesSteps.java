package com.example.api.steps;

import com.example.api.api.ZonesAPI;
import com.example.api.context.TestScenarioContext;
import com.example.api.utils.TestFixtures;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class ZonesSteps {
    private final TestScenarioContext context = TestScenarioContext.current();

    @Given("User in zone Page")
    public void authenticateAdministrator() {
        TestFixtures.loginAdmin(context);
        context.zoneCode = "ZONE-" + Long.toString(System.nanoTime(), 36).toUpperCase();
    }

    @When("User enters zones details")
    public void createZone() {
        context.response = ZonesAPI.createZone(context.token, context.zoneCode,
                "QA Parking Zone", "Dublin", "Created by API automation", true);
    }

    @Then("User created zone successfully")
    public void verifyZone() {
        TestFixtures.requireStatus(context.response, 201, "create zone");
        Assert.assertTrue(context.response.jsonPath().getInt("id") > 0);
        Assert.assertEquals(context.response.jsonPath().getString("code"), context.zoneCode);
        Assert.assertTrue(context.response.jsonPath().getBoolean("active"));
    }
}
