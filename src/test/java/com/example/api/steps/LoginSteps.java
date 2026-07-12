package com.example.api.steps;

import com.example.api.api.LoginAPI;
import com.example.api.context.TestScenarioContext;
import com.example.api.utils.TestFixtures;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LoginSteps {
    private final TestScenarioContext context = TestScenarioContext.current();

    @Given("User in Login Page")
    public void registerLoginUser() {
        TestFixtures.createAdmin(context);
    }

    @When("User enters username and password")
    public void login() {
        context.response = LoginAPI.login(context.username, context.password);
    }

    @Then("User logins successfully")
    public void verifyLogin() {
        TestFixtures.requireStatus(context.response, 200, "login user");
        Assert.assertEquals(context.response.jsonPath().getString("tokenType"), "Bearer");
        Assert.assertEquals(context.response.jsonPath().getString("username"), context.username);
        Assert.assertFalse(context.response.jsonPath().getString("accessToken").isBlank());
        Assert.assertTrue(context.response.jsonPath().getList("roles", String.class).contains("ADMIN"));
    }
}
