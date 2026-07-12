Feature: location

  @location @regression
  Scenario: validate with valid locations
    Given User in location Page
    When User enters location details
    Then User created location successfully

  @locationspace @regression
  Scenario: validate with valid locationSpace
    Given User in locationSpace Page
    When User enters locationSpace details
    Then User created locationSpace successfully