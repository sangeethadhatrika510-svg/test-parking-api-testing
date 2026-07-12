package com.example.api.steps;

import com.example.api.api.LocationAPI;
import com.example.api.context.TestScenarioContext;
import com.example.api.utils.TestFixtures;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LocationSteps {
    private final TestScenarioContext context = TestScenarioContext.current();

    @Given("User in location Page")
    public void createLocationPrerequisites() {
        TestFixtures.createZone(context);
        context.locationName = "QA Location " + Long.toString(System.nanoTime(), 36);
    }

    @When("User enters location details")
    public void createLocation() {
        context.response = LocationAPI.createLocation(context.token, context.zoneId,
                context.locationName, "1 Test Street", "UNMAPPED_AREA", true);
    }

    @Then("User created location successfully")
    public void verifyLocation() {
        TestFixtures.requireStatus(context.response, 201, "create location");
        Assert.assertTrue(context.response.jsonPath().getInt("id") > 0);
        Assert.assertEquals(context.response.jsonPath().getInt("zoneId"), context.zoneId);
        Assert.assertEquals(context.response.jsonPath().getString("name"), context.locationName);
    }

    @Given("User in locationSpace Page")
    public void createSpacePrerequisites() {
        TestFixtures.createLocation(context);
        context.spaceCode = "SPACE-" + Long.toString(System.nanoTime(), 36).toUpperCase();
    }

    @When("User enters locationSpace details")
    public void createSpace() {
        context.response = LocationAPI.createSpace(
                context.token, context.locationId, context.spaceCode, "AVAILABLE", true);
    }

    @Then("User created locationSpace successfully")
    public void verifySpace() {
        TestFixtures.requireStatus(context.response, 201, "create parking space");
        Assert.assertTrue(context.response.jsonPath().getInt("id") > 0);
        Assert.assertEquals(context.response.jsonPath().getInt("locationId"), context.locationId);
        Assert.assertEquals(context.response.jsonPath().getString("code"), context.spaceCode);
        Assert.assertEquals(context.response.jsonPath().getString("status"), "AVAILABLE");
        Assert.assertTrue(context.response.jsonPath().getBoolean("reservable"));
    }
}
