package com.example.api.api;

import com.example.api.config.TestConfig;
import com.example.api.payload.LoginPayload;
import com.example.api.utils.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class LoginAPI {
    public static Response loginResponse(){
        RestAssured.baseURI= BaseTest.authURL;

        return given().log().all()
                .header("Content-Type", "application/json")
                .body(LoginPayload.loginpayload(TestConfig.get("test.username"),TestConfig.get("test.password")))
                .when().post("/auth/login");

    }
}
