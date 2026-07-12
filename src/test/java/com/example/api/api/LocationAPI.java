package com.example.api.api;

import com.example.api.config.TestConfig;
import com.example.api.payload.LocationPayload;
import com.example.api.payload.LocationSpacePayload;
import com.example.api.utils.AuthUtil;
import com.example.api.utils.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.specification.ProxySpecification.auth;

public class LocationAPI {
    public static Response locationResponse() {
        RestAssured.baseURI = BaseTest.locURL;
        return given().log().all().header("Content-Type", "application/json")
                .body(LocationPayload.locationPayload
                        (AuthUtil.zoneId, TestConfig.get("location.name"), TestConfig.get("location.address"),
                                TestConfig.get("location.type"), Boolean.parseBoolean(TestConfig.get("test.active"))))
                .auth().oauth2(AuthUtil.bearerToken).when().post("/parking-locations");
    }

    public static Response locationSpaceResponse() {
        RestAssured.baseURI = BaseTest.locURL;
        String path = "parking-locations/"+AuthUtil.locationId+"/spaces";
        return given().log().all().header("Content-Type", "application/json")
                .body(LocationSpacePayload.locationSpacePayload(
                        TestConfig.get("space.code"),TestConfig.get("space.status"),
                        Boolean.parseBoolean(TestConfig.get("space.reservable")))).auth().oauth2(AuthUtil.bearerToken).when().post(path);

    }
}