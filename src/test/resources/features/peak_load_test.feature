@performance
Feature: ReqRes API Integration Test

  Background:
    Given base URL is "https://reqres.in"
    And users count is 1
    And ramp-up time is 1 seconds

  Scenario: API GET with metrics
    When user makes concurrent GET requests to "/api/users?page=2"
    Then all responses should have status code 200
    And response time should be less than 5000 ms
    And record metric "GET /api/users - Page2"

  Scenario: API DELETE user
    When user makes concurrent GET requests to "/api/users/1"
    Then all responses should have status code 200
    And response time should be less than 5000 ms
    And record metric "GET /api/users/1 - Final"
