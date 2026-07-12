package com.example.api.api;

import com.example.api.payload.ZonesPayload;
import com.example.api.utils.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;


public class ZonesAPI {
    public static Response createZone(String token, String code, String name, String city,
                                      String description, boolean active) {
        RestAssured.baseURI = BaseTest.locationUrl();
        return given().header("Content-Type", "application/json")
                .body(ZonesPayload.zonePayload(code, name, city, description, active))
                .auth().oauth2(token).when().post("/zones");
    }
}
