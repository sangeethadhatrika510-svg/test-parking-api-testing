package com.example.api.api;

import com.example.api.payload.RegisterPayload;
import com.example.api.utils.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.List;

import static io.restassured.RestAssured.given;

public class RegisterAPI {

    public static Response register(String username, String email, String password, List<String> roles) {
        RestAssured.baseURI = BaseTest.authUrl();
        return given()
                .header("Content-Type", "application/json")
                .body(RegisterPayload.registerPayload(username, email, password, roles))
                .when().post("/auth/register");
    }
}
