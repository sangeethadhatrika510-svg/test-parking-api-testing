package com.example.api.api;

import com.example.api.config.TestConfig;
import com.example.api.payload.ZonesPayload;
import com.example.api.utils.AuthUtil;
import com.example.api.utils.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;


public class ZonesAPI {
    public static Response zoneResponse() {
        RestAssured.baseURI = BaseTest.locURL;
        return given().log().all().header("Content-Type", "application/json")
                .body(ZonesPayload.zonePayload(TestConfig.get("test.code"),TestConfig.get("test.name"),
                        TestConfig.get("test.city"),TestConfig.get("test.description"),
                                Boolean.parseBoolean(TestConfig.get("test.active"))))
                .auth().oauth2(AuthUtil.bearerToken).when().post("/zones");

    }
}