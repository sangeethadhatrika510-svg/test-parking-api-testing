package com.example.api.steps;

import com.example.api.api.RegisterAPI;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;

import static org.junit.Assert.assertEquals;

public class RegisterSteps {
     static Response res;
    @Given("User in register Page")
    public void user_in_register_page() {
        System.out.println("user provided register details");

    }
    @When("User enters register details")
    public void user_enters_register_details() throws JsonProcessingException {
        res=RegisterAPI.registerResponse();
        int id= res.jsonPath().get("id");
        System.out.println(id);


    }
    @Then("User registers successfully")
    public void user_registers_successfully() {
        assertEquals(201,res.getStatusCode());

    }
}
