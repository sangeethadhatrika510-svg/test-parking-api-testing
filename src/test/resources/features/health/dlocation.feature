Feature: location

  @location @smoke @regression
  Scenario: validate with valid locations
    Given User in location Page
    When User enters location details
    Then User created location successfully

  @location @locationspace @regression
  Scenario: validate with valid locationSpace
    Given User in locationSpace Page
    When User enters locationSpace details
    Then User created locationSpace successfully
