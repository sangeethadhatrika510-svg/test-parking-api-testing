Feature: Login

  @auth @smoke @regression @login
  Scenario: validate with valid credentials
    Given User in Login Page
    When User enters username and password
    Then User logins successfully
