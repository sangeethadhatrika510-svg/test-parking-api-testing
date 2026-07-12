package com.example.api.steps;

import com.example.api.api.RegisterAPI;
import com.example.api.context.TestScenarioContext;
import com.example.api.utils.TestFixtures;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.testng.Assert;

public class RegisterSteps {
    private final TestScenarioContext context = TestScenarioContext.current();

    @Given("User in register Page")
    public void prepareUniqueRegistrationData() {
        String suffix = Long.toString(System.nanoTime(), 36);
        context.username = "qa_" + suffix;
        context.email = context.username + "@example.test";
        context.password = "Password123!";
    }

    @When("User enters register details")
    public void registerUser() {
        context.response = RegisterAPI.register(
                context.username, context.email, context.password, List.of("ADMIN", "USER"));
    }

    @Then("User registers successfully")
    public void verifyRegistration() {
        TestFixtures.requireStatus(context.response, 201, "register user");
        Assert.assertEquals(context.response.jsonPath().getString("username"), context.username);
        Assert.assertTrue(context.response.jsonPath().getList("roles", String.class).contains("ADMIN"));
        Assert.assertNull(context.response.jsonPath().get("password"), "Registration response exposed password");
    }
}
