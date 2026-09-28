@performance
Feature: ReqRes API Baseline Load Test

  Background:
    Given base URL is "https://reqres.in"
    And users count is 10
    And ramp-up time is 30 seconds

  Scenario: Load test GET users list
    When user makes concurrent GET requests to "/api/users?page=1"
    Then all responses should have status code 200
    And response time should be less than 500 ms
    And throughput should be greater than 20 RPS
    And record metric "GET /api/users - Baseline"

  Scenario: Load test POST create user
    When user makes concurrent POST requests to "/api/users" with body
      | name | job      |
      | John | QA Eng   |
    Then all responses should have status code 201
    And response time should be less than 1000 ms
    And error rate should be less than 1%
    And record metric "POST /api/users - Baseline"

  Scenario: Load test GET single user
    When user makes concurrent GET requests to "/api/users/1"
    Then all responses should have status code 200
    And response time should be less than 500 ms
    And active threads count should match 10
    And record metric "GET /api/users/{id} - Baseline"

  Scenario: Load test PUT update user
    When user makes concurrent PUT requests to "/api/users/1" with body
      | name | job         |
      | Jane | Senior QA   |
    Then all responses should have status code 200
    And response time should be less than 800 ms
    And record metric "PUT /api/users/{id} - Baseline"
