@performance
Feature: ReqRes API Baseline Load Test

  Background:
    Given base URL is "https://reqres.in"
    And users count is 1
    And ramp-up time is 1 seconds

  Scenario: API GET users list
    When user makes concurrent GET requests to "/api/users?page=1"
    Then all responses should have status code 200
    And response time should be less than 5000 ms
    And record metric "GET /api/users"

  Scenario: API POST create user
    When user makes concurrent POST requests to "/api/users" with body
      | name | job      |
      | John | QA Eng   |
    Then all responses should have status code 201
    And response time should be less than 5000 ms
    And record metric "POST /api/users"

  Scenario: API GET single user
    When user makes concurrent GET requests to "/api/users/1"
    Then all responses should have status code 200
    And response time should be less than 5000 ms
    And record metric "GET /api/users/1"

  Scenario: API PUT update user
    When user makes concurrent PUT requests to "/api/users/1" with body
      | name | job         |
      | Jane | Senior QA   |
    Then all responses should have status code 200
    And response time should be less than 5000 ms
    And record metric "PUT /api/users/1"
