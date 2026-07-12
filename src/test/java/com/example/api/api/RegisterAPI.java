package com.example.api.api;

import com.example.api.config.TestConfig;
import com.example.api.payload.RegisterPayload;
import com.example.api.utils.BaseTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;

public class RegisterAPI {

    public static Response registerResponse() throws JsonProcessingException {
        RestAssured.baseURI = BaseTest.authURL;
        ArrayList<String> roles = new ArrayList<>();
        roles.add("ADMIN");

        return given().log().all()
                .header("Content-Type", "application/json")
                .body(RegisterPayload.registerpayload(TestConfig.get("test.username"),
                        TestConfig.get("test.email"),
                        TestConfig.get("test.password"),
                        roles)).when().post("/auth/register");
    }


}
