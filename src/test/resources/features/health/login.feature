Feature: Login

  Scenario: validate with valid credentials
    Given User in Login Page
    When User enters username and password
    Then User logins successfully
