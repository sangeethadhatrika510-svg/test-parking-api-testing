package com.example.api.api;

import com.example.api.payload.LocationPayload;
import com.example.api.payload.LocationSpacePayload;
import com.example.api.utils.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class LocationAPI {
    public static Response createLocation(String token, int zoneId, String name, String address,
                                          String type, boolean active) {
        RestAssured.baseURI = BaseTest.locationUrl();
        return given().header("Content-Type", "application/json")
                .body(LocationPayload.locationPayload(zoneId, name, address, type, active))
                .auth().oauth2(token).when().post("/parking-locations");
    }

    public static Response createSpace(String token, int locationId, String code,
                                       String status, boolean reservable) {
        RestAssured.baseURI = BaseTest.locationUrl();
        String path = "/parking-locations/" + locationId + "/spaces";
        return given().header("Content-Type", "application/json")
                .body(LocationSpacePayload.locationSpacePayload(code, status, reservable))
                .auth().oauth2(token).when().post(path);
    }
}
