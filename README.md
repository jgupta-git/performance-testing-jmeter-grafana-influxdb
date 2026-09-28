# Performance Testing — Cucumber + RestAssured + Grafana + InfluxDB

BDD-based load and performance testing framework for API performance analysis with real-time visualization.

## Tech Stack

Cucumber 7.14.0 | RestAssured 5.3.2 | InfluxDB Cloud | Grafana Cloud | ReqRes API

## Overview

**Test Target:** [ReqRes API](https://reqres.in) — Free mock API for testing  
**Testing Approach:** Cucumber BDD (Gherkin syntax)  
**API Testing:** RestAssured  
**Metrics Storage:** InfluxDB Cloud (30-day free tier)  
**Visualization:** Grafana Cloud (unlimited free dashboards)

## Test Scenarios (2 Load Profiles)

### Baseline Load Test (10 users)

| Scenario | Endpoint | Method | Assertion | Target |
|----------|----------|--------|-----------|--------|
| GET users list | `/api/users?page=1` | GET | 200 OK | < 500ms |
| Create user | `/api/users` | POST | 201 Created | < 1000ms |
| Fetch single user | `/api/users/1` | GET | 200 OK | < 500ms |
| Update user | `/api/users/1` | PUT | 200 OK | < 800ms |

### Peak Load Test (50 users)

| Scenario | Load | Duration | Ramp-up | Target |
|----------|------|----------|---------|--------|
| GET requests | 50 concurrent | 5 min | 60s | < 800ms (p95) |
| POST requests | 50 concurrent | 5 min | 60s | < 1500ms (p95) |
| Mixed operations | 50 concurrent | 5 min | 60s | < 600ms avg |

## Performance Baseline (Local)

| Metric | Baseline | Target |
|--------|----------|--------|
| **Avg Response Time** | 250-350ms | < 500ms |
| **95th Percentile** | 400-600ms | < 1000ms |
| **Throughput (RPS)** | 30-50 | > 20 RPS |
| **Error Rate** | 0% | < 1% |
| **Max Concurrent Users** | 100 | > 50 (free tier) |

## Project Structure

```
performance-testing-jmeter-grafana-influxdb/
├── pom.xml                                    # Maven configuration
├── README.md                                  # This file
├── INFLUXDB_SETUP.md                         # InfluxDB configuration guide
├── GRAFANA_SETUP.md                          # Grafana dashboard setup
├── QUICK_START.md                            # Quick start guide
├── src/
│   └── test/
│       ├── java/com/performance/testing/
│       │   ├── steps/
│       │   │   └── ApiPerformanceSteps.java  # Cucumber step definitions
│       │   ├── config/
│       │   │   └── PerformanceMetrics.java   # InfluxDB integration
│       │   └── runners/
│       │       └── PerformanceTestRunner.java # Cucumber runner
│       └── resources/
│           ├── features/
│           │   ├── baseline_load_test.feature
│           │   └── peak_load_test.feature
│           └── cucumber.properties
└── target/
    ├── cucumber-reports/                     # JSON results
    └── cucumber-html-reports/                # HTML reports
```

## Key Files

### Feature Files (Gherkin)
- **[baseline_load_test.feature](src/test/resources/features/baseline_load_test.feature)** — 10 user load scenarios
- **[peak_load_test.feature](src/test/resources/features/peak_load_test.feature)** — 50 user load scenarios

### Step Definitions (Java)
- **[ApiPerformanceSteps.java](src/test/java/com/performance/testing/steps/ApiPerformanceSteps.java)** — HTTP requests, assertions, metrics collection
- **[PerformanceMetrics.java](src/test/java/com/performance/testing/config/PerformanceMetrics.java)** — InfluxDB client integration

## Quick Start

### 1. Prerequisites

- ✅ **Java 11+** — Required for Cucumber/RestAssured
- ✅ **Maven 3.6+** — Build and test orchestration
- ✅ **InfluxDB API Token** — See [INFLUXDB_SETUP.md](INFLUXDB_SETUP.md)
- ✅ **Grafana Account** — Already set up at https://cordialmustard1705.grafana.net

### 2. Set Up InfluxDB

Follow [INFLUXDB_SETUP.md](INFLUXDB_SETUP.md):

```bash
# 1. Create bucket: jmeter-metrics
# 2. Generate API token
# 3. Store token in environment variable:

# Windows (PowerShell):
$env:INFLUXDB_URL = "https://us-east-1-1.aws.cloud2.influxdata.com"
$env:INFLUXDB_TOKEN = "your-api-token-here"
$env:INFLUXDB_ORG = "30a6ee34d37ea258"
$env:INFLUXDB_BUCKET = "jmeter-metrics"

# Linux/Mac:
export INFLUXDB_URL="https://us-east-1-1.aws.cloud2.influxdata.com"
export INFLUXDB_TOKEN="your-api-token-here"
export INFLUXDB_ORG="30a6ee34d37ea258"
export INFLUXDB_BUCKET="jmeter-metrics"
```

### 3. Set Up Grafana

Follow [GRAFANA_SETUP.md](GRAFANA_SETUP.md):

```
1. Connect InfluxDB data source to Grafana
2. Create real-time metrics dashboard
3. Create performance summary dashboard
```

### 4. Run Performance Tests

#### Run All Tests:

```bash
mvn clean test
```

#### Run Baseline Tests Only:

```bash
mvn clean test -Dcucumber.features="src/test/resources/features/baseline_load_test.feature"
```

#### Run Peak Load Tests Only:

```bash
mvn clean test -Dcucumber.features="src/test/resources/features/peak_load_test.feature"
```

### 5. View Results

**HTML Report (Local):**
```
target/cucumber-html-reports/index.html
```

**InfluxDB (Cloud):**
```
https://us-east-1-1.aws.cloud2.influxdata.com/orgs/30a6ee34d37ea258
```

**Grafana Dashboards (Cloud):**
```
https://cordialmustard1705.grafana.net/d/<dashboard-id>
```

---

## BDD Feature Examples

### Baseline Load Test Feature

```gherkin
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
```

### Peak Load Test Feature

```gherkin
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
```

---

## Step Definitions Reference

### Given Steps (Setup)
```java
Given base URL is "https://reqres.in"
And users count is 10
And ramp-up time is 30 seconds
```

### When Steps (Execution)
```java
When user makes concurrent GET requests to "/api/users?page=1"
When user makes concurrent POST requests to "/api/users" with body
When user makes concurrent PUT requests to "/api/users/1" with body
When user executes mixed operations for 300 seconds
```

### Then Steps (Assertions)
```java
Then all responses should have status code 200
Then response time should be less than 500 ms
Then 95th percentile response time should be less than 800 ms
Then throughput should be greater than 20 RPS
Then error rate should be less than 1%
Then active threads count should match 10
Then system should sustain 50 concurrent users
Then record metric "GET /api/users - Baseline"
```

---

## InfluxDB Integration

Metrics automatically sent to InfluxDB on each test run:

### Measurement: `performance`

**Tags:**
- `test_name` — Feature/scenario name
- `environment` — "cucumberbdd"

**Fields:**
- `total_requests` — Total API calls
- `error_count` — Failed requests
- `error_rate` — Error percentage
- `avg_response_time` — Average latency (ms)
- `min_response_time` — Minimum latency
- `max_response_time` — Maximum latency
- `p95_response_time` — 95th percentile
- `active_threads` — Concurrent users
- `throughput` — Requests per second

### Example Data Point:
```
performance,test_name="GET /api/users - Baseline",environment="cucumberbdd"
total_requests=200i,error_count=0i,error_rate=0.0,avg_response_time=350i,
p95_response_time=550i,active_threads=10i,throughput=40.0
```

---

## Grafana Dashboard Metrics

### Real-Time Monitoring

**Dashboard 1:** Performance - Real-Time Metrics
- Response time over time (by test scenario)
- Requests per second (throughput)
- Error count timeline
- Active threads visualization
- Min/Max/Avg response times

**Dashboard 2:** Performance - Summary
- Average response time (current + trend)
- 95th percentile response time
- Success rate (gauge)
- Total requests counter
- Error rate percentage

### Query Examples

**Average response time by test:**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "performance" and r._field == "avg_response_time")
  |> group(by: ["test_name"])
```

**Throughput over time:**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._field == "throughput")
  |> group(by: ["test_name"])
```

**Error rate by test:**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._field == "error_rate")
  |> group(by: ["test_name"])
```

---

## CI/CD Integration

### Jenkins Pipeline Example

```groovy
pipeline {
  stages {
    stage('Performance Test - Baseline') {
      steps {
        sh 'mvn clean test -Dcucumber.features="src/test/resources/features/baseline_load_test.feature"'
      }
    }
    stage('Performance Test - Peak Load') {
      steps {
        sh 'mvn clean test -Dcucumber.features="src/test/resources/features/peak_load_test.feature"'
      }
    }
    stage('Publish Results') {
      steps {
        publishHTML([
          reportDir: 'target/cucumber-html-reports',
          reportFiles: 'index.html',
          reportName: 'Cucumber Performance Report'
        ])
        archiveArtifacts artifacts: 'target/cucumber-reports/cucumber.json'
      }
    }
  }
}
```

### GitHub Actions Example

```yaml
- name: Run Baseline Performance Tests
  env:
    INFLUXDB_URL: ${{ secrets.INFLUXDB_URL }}
    INFLUXDB_TOKEN: ${{ secrets.INFLUXDB_TOKEN }}
    INFLUXDB_ORG: ${{ secrets.INFLUXDB_ORG }}
    INFLUXDB_BUCKET: ${{ secrets.INFLUXDB_BUCKET }}
  run: |
    mvn clean test -Dcucumber.features="src/test/resources/features/baseline_load_test.feature"

- name: Upload Results
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: performance-report
    path: target/cucumber-html-reports/
```

---

## Troubleshooting

### Tests Won't Run

**Error:** "Cannot find step definitions"  
**Fix:** Ensure glue path in runner matches step definitions package:
```java
@CucumberOptions(
    glue = {"com.performance.testing.steps"}
)
```

### No Metrics in InfluxDB

**Error:** Metrics not appearing in Grafana  
**Cause:** InfluxDB credentials not set  
**Fix:**
```bash
# Verify environment variables
echo $INFLUXDB_TOKEN
echo $INFLUXDB_URL
echo $INFLUXDB_ORG
echo $INFLUXDB_BUCKET

# Run test again
mvn clean test
```

### Connection Timeout to ReqRes API

**Error:** "Connection refused" or "timeout"  
**Cause:** Internet connectivity or API endpoint down  
**Fix:**
```bash
# Test API manually
curl https://reqres.in/api/users

# Reduce load if API is overloaded
mvn clean test -Dusers.count=5
```

### Out of Memory Error

**Error:** "Exception in thread... OutOfMemoryError"  
**Cause:** JVM heap too small for large tests  
**Fix:**
```bash
export MAVEN_OPTS="-Xmx2g"
mvn clean test
```

---

## Performance Optimization Tips

1. **Optimize Response Time:**
   - Increase server resources
   - Cache frequently accessed data
   - Use CDN for static content

2. **Increase Throughput:**
   - Connection pooling
   - Parallel request processing
   - Load balancing across instances

3. **Reduce Errors:**
   - Add retry logic for transient failures
   - Monitor system resource limits
   - Implement circuit breaker patterns

4. **Scale Load Testing:**
   - Increase thread count gradually
   - Test on staging environment
   - Monitor system metrics during test

---

## References

- **Apache Cucumber:** [cucumber.io](https://cucumber.io/)
- **RestAssured:** [rest-assured.io](https://rest-assured.io/)
- **InfluxDB Cloud:** [influxdata.com](https://www.influxdata.com/)
- **Grafana Cloud:** [grafana.com](https://grafana.com/)
- **ReqRes API:** [reqres.in](https://reqres.in)

---

## GitHub Repository

```
https://github.com/jgupta-git/performance-testing-jmeter-grafana-influxdb
```

---

**Last Updated:** 2026-09-28 | Built with Cucumber • RestAssured • InfluxDB • Grafana
