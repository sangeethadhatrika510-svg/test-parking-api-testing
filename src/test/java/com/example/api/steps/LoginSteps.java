package com.example.api.steps;

import com.example.api.api.LoginAPI;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;

import static org.junit.Assert.assertEquals;

public class LoginSteps {
    static Response loginJsonResponse;
    public static String accessToken;
    @Given("User in Login Page")
    public void userInLoginPage() {
        System.out.println("User login details provided");
    }
    @When("User enters username and password")
    public void userEntersUsernameAndPassword() {
        loginJsonResponse= LoginAPI.loginResponse();
        accessToken=loginJsonResponse.getBody().jsonPath().getString("accessToken");
        System.out.println("Data "+ loginJsonResponse.toString());
        System.out.println("AccessToken "+ accessToken);
        System.out.println("ResponseTime "+ loginJsonResponse.getTime());
    }
    @Then("User logins successfully")
    public void userLoginsSuccessfully() {
        assertEquals(200,loginJsonResponse.getStatusCode());
    }
}
