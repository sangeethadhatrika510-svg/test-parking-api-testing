package com.example.api.steps;

import com.example.api.api.LocationAPI;
import com.example.api.api.LoginAPI;
import com.example.api.payload.LocationSpacePayload;
import com.example.api.utils.AuthUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.junit.Assert.assertEquals;


public class LocationSteps {
public static  Response locResponse;
    public static  Response locSpaceResponse;

    @Given("User in location Page")
    public void user_in_location_page() {

        System.out.println("user provided location details");
    }

    @When("User enters location details")
    public void user_enters_location_details() {
      locResponse= LocationAPI.locationResponse();
      int locID=locResponse.jsonPath().getInt("id");

    }

    @Then("User created location successfully")
    public void user_created_location_successfully() {
        assertEquals(201,locResponse.getStatusCode());

    }
    @Given("User in locationSpace Page")
    public void user_in_locationSpace_page() {
        System.out.println("user provided location space details");
    }
    @When("User enters locationSpace details")
    public void user_enters_locationSpace_details() {
        locSpaceResponse= LocationAPI.locationSpaceResponse();

        int spaceID=locSpaceResponse.jsonPath().getInt("id");

    }
    @Then("User created locationSpace successfully")
    public void user_created_locationSpace_successfully() {

    }


}
