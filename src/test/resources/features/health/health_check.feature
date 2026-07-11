@health
Feature: API health check

  @smoke @regression
  Scenario: API status endpoint should be available
    When I send a GET request to "/status"
    Then the response status code should be 200
