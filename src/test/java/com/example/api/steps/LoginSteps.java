package com.example.api.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class LoginSteps {

    @Given("User in Login Page")
    public void userInLoginPage() {
        System.out.println("User in application page");
    }
    @When("User enters username and password")
    public void userEntersUsernameAndPassword() {
        System.out.println("User entered valid credientials");
    }
    @Then("User logins successfully")
    public void userLoginsSuccessfully() {
        System.out.println("User successfully logins");
    }
}
