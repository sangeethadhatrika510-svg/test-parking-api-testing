package com.example.api.utils;

import com.example.api.api.LocationAPI;
import com.example.api.api.LoginAPI;
import com.example.api.api.ZonesAPI;
import io.cucumber.java.Before;
import io.restassured.response.Response;

public class AuthUtil {
    public static String bearerToken;
    public static int zoneId=0;
    public static int locationId=0;

    @Before("@zones or @location or @locationspace")
    public void getToken() {
       Response loginJsonResponse= LoginAPI.loginResponse();
       bearerToken=loginJsonResponse.getBody().jsonPath().getString("accessToken");
    }
    @Before("@location")
    public void zoneId(){
        if(zoneId == 0){
            Response zoneRes= ZonesAPI.zoneResponse();
            zoneId=zoneRes.jsonPath().getInt("id");
        }
    }
    @Before("@locationspace")
    public void locationId(){
        if(locationId == 0){
            Response locRes= LocationAPI.locationResponse();
            locationId=locRes.jsonPath().getInt("id");
        }
    }
}