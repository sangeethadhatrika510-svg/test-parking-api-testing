package com.example.api.steps;

import com.example.api.api.ZonesAPI;
import com.example.api.utils.AuthUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.junit.Assert.assertEquals;

public class ZonesSteps {
    static Response zoneRes;
    @Given("User in zone Page")
    public void user_in_zone_page() {
       System.out.println("User provided zone details");

    }
    @When("User enters zones details")
    public void user_enters_zones_details() {
         zoneRes= ZonesAPI.zoneResponse();
         int id=zoneRes.jsonPath().getInt("id");
        AuthUtil.zoneId=id;
        System.out.println("ID " + id);


    }
    @Then("User created zone successfully")
    public void user_created_zone_successfully() {
        assertEquals(201,zoneRes.getStatusCode());

    }
}
