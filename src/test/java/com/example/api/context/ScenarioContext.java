package com.example.api.context;

import io.restassured.response.Response;

public class ScenarioContext {
    private Response response;

    public Response response() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
