Feature: Register

  @smoke @regression @register
  Scenario: valid register details
    Given User in register Page
    When User enters register details
    Then User registers successfully