package com.example.api.api;

import com.example.api.payload.LoginPayload;
import com.example.api.utils.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class LoginAPI {
    public static Response login(String username, String password) {
        RestAssured.baseURI = BaseTest.authUrl();

        return given()
                .header("Content-Type", "application/json")
                .body(LoginPayload.loginPayload(username, password))
                .when().post("/auth/login");
    }
}
