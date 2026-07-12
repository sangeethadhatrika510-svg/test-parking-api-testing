Feature: Zones

  @location @smoke @regression @zones
  Scenario: validate with valid zones
    Given User in zone Page
    When User enters zones details
    Then User created zone successfully
