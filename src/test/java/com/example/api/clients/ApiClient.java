package com.example.api.clients;

import com.example.api.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ApiClient {
    private final RequestSpecification requestSpecification;

    public ApiClient() {
        RestAssuredConfig config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", TestConfig.requestTimeoutMs())
                        .setParam("http.socket.timeout", TestConfig.requestTimeoutMs()));

        this.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(TestConfig.baseUrl())
                .setConfig(config)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    public Response get(String path) {
        return RestAssured.given()
                .spec(requestSpecification)
                .when()
                .get(path)
                .then()
                .extract()
                .response();
    }

    public Response post(String path, Object body) {
        return RestAssured.given()
                .spec(requestSpecification)
                .body(body)
                .when()
                .post(path)
                .then()
                .extract()
                .response();
    }
}
