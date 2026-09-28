@performance
Feature: ReqRes API Peak Load Test

  Background:
    Given base URL is "https://reqres.in"
    And users count is 50
    And ramp-up time is 60 seconds

  Scenario: Peak load - GET users list
    When user makes concurrent GET requests to "/api/users?page=1"
    Then all responses should have status code 200
    And 95th percentile response time should be less than 800 ms
    And throughput should be greater than 15 RPS
    And error rate should be less than 2%
    And record metric "GET /api/users - Peak Load"

  Scenario: Peak load - POST create user
    When user makes concurrent POST requests to "/api/users" with body
      | name | job        |
      | Peak | Tester     |
    Then all responses should have status code 201
    And 95th percentile response time should be less than 1500 ms
    And record metric "POST /api/users - Peak Load"

  Scenario: Peak load - Mixed operations
    When user executes mixed operations for 300 seconds
      | method | endpoint         | percentage |
      | GET    | /api/users?page=1| 40         |
      | POST   | /api/users       | 30         |
      | GET    | /api/users/1     | 20         |
      | PUT    | /api/users/1     | 10         |
    Then average response time should be less than 600 ms
    And system should sustain 50 concurrent users
    And record metric "Mixed Operations - Peak Load"
