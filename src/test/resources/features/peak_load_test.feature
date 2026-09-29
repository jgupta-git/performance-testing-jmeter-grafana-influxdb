@performance
Feature: ReqRes API Integration Test

  Background:
    Given base URL is "https://reqres.in"
    And users count is 1
    And ramp-up time is 1 seconds

  Scenario: API GET with metrics
    When user makes concurrent GET requests to "/api/users?page=2"
    Then response time should be less than 5000 ms
    And record metric "GET /api/users - Page2"

  Scenario: API monitoring test
    When user makes concurrent GET requests to "/api/users/1"
    Then response time should be less than 5000 ms
    And record metric "GET /api/users/1 - Monitoring"
