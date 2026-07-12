package com.example.api.steps;

import com.example.api.clients.ApiClient;
import com.example.api.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class CommonApiSteps {
    private final ApiClient apiClient;
    private final ScenarioContext context;

    public CommonApiSteps() {
        this.apiClient = new ApiClient();
        this.context = SharedScenarioContext.context();
    }

    @When("I send a GET request to {string}")
    public void iSendAGetRequestTo(String path) {
        context.setResponse(apiClient.get(path));
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatusCode) {
        Assert.assertNotNull(context.response(), "No API response is available in scenario context");
        Assert.assertEquals(context.response().statusCode(), expectedStatusCode);
    }
}
