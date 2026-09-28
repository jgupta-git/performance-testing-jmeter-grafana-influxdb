# Quick Start Guide

## 📋 Project Updated: Performance Testing with Cucumber BDD

This portfolio project now demonstrates:
- ✅ Cucumber BDD (Gherkin syntax) for performance testing
- ✅ RestAssured for API testing
- ✅ Real-time metrics collection with InfluxDB Cloud
- ✅ Performance visualization with Grafana dashboards
- ✅ Concurrent load scenarios (10, 50+ users)
- ✅ CI/CD integration ready (Jenkins/GitHub Actions)

---

## 📁 Project Structure

```
performance-testing-jmeter-grafana-influxdb/
├── pom.xml                                          # Maven + Cucumber config
├── README.md                                        # Complete documentation
├── QUICK_START.md                                   # This file
├── INFLUXDB_SETUP.md                               # InfluxDB setup
├── GRAFANA_SETUP.md                                # Grafana dashboard setup
├── .gitignore                                       # Git security
└── src/test/
    ├── java/com/performance/testing/
    │   ├── steps/
    │   │   └── ApiPerformanceSteps.java             # Step definitions
    │   ├── config/
    │   │   └── PerformanceMetrics.java              # InfluxDB integration
    │   └── runners/
    │       └── PerformanceTestRunner.java           # Test runner
    └── resources/
        ├── features/
        │   ├── baseline_load_test.feature           # 10-user scenarios
        │   └── peak_load_test.feature               # 50-user scenarios
        └── cucumber.properties
```

---

## ⚡ 5-Minute Setup

### Step 1: Gather InfluxDB Information

1. Go to: https://us-east-1-1.aws.cloud2.influxdata.com
2. Follow **[INFLUXDB_SETUP.md](INFLUXDB_SETUP.md)** to:
   - ✅ Create bucket: `jmeter-metrics`
   - ✅ Generate API token
   - ✅ Store token in environment variables:
   ```bash
   # Windows PowerShell:
   $env:INFLUXDB_URL = "https://us-east-1-1.aws.cloud2.influxdata.com"
   $env:INFLUXDB_TOKEN = "your-token-here"
   $env:INFLUXDB_ORG = "30a6ee34d37ea258"
   $env:INFLUXDB_BUCKET = "jmeter-metrics"
   
   # Linux/Mac:
   export INFLUXDB_URL="https://us-east-1-1.aws.cloud2.influxdata.com"
   export INFLUXDB_TOKEN="your-token-here"
   export INFLUXDB_ORG="30a6ee34d37ea258"
   export INFLUXDB_BUCKET="jmeter-metrics"
   ```

### Step 2: Set Up Grafana Dashboards

1. Go to: https://cordialmustard1705.grafana.net
2. Follow **[GRAFANA_SETUP.md](GRAFANA_SETUP.md)** to:
   - ✅ Add InfluxDB data source
   - ✅ Create 2 dashboards (Real-Time + Summary)
   - ✅ Test data source connection

### Step 3: Run First Test

```bash
mvn clean test
```

Expected output:
```
[INFO] Running Performance Test Runner
[INFO] 1 Scenarios (1 passed)
[INFO] 4 Steps (4 passed)
[INFO] Metrics sent to InfluxDB successfully
[INFO] BUILD SUCCESS
```

### Step 4: View Results

**Option A - Local HTML Report:**
```
target/cucumber-html-reports/index.html
```
Shows:
- Feature execution status (✓ passed/✗ failed)
- Each step with results and attachments
- Performance metrics collected during test
- Test duration and timeline

**Option B - Grafana Cloud (Live):**
```
https://cordialmustard1705.grafana.net/d/<dashboard-id>
```
Look for:
- 📊 Response time graphs
- 📈 Throughput (RPS) metrics
- ⚠️ Error counts
- 👥 Active threads

---

## 🧪 What Gets Tested

### Baseline Load Test (10 users)

| Endpoint | Method | Purpose | Target |
|----------|--------|---------|--------|
| `/api/users?page=1` | GET | List users | < 500ms |
| `/api/users` | POST | Create user | < 1000ms |
| `/api/users/1` | GET | Fetch single user | < 500ms |
| `/api/users/1` | PUT | Update user | < 800ms |

### Peak Load Test (50 users)

| Endpoint | Method | Users | Duration | Target |
|----------|--------|-------|----------|--------|
| `/api/users?page=1` | GET | 50 | 5 min | < 800ms (p95) |
| `/api/users` | POST | 50 | 5 min | < 1500ms (p95) |
| Mixed | Mixed | 50 | 5 min | < 600ms avg |

---

## 📊 Metrics Collected

JMeter automatically sends these to InfluxDB:

| Metric | Example | Use Case |
|--------|---------|----------|
| **Total Requests** | 200 | Overall test volume |
| **Error Count** | 2 | Failed requests |
| **Error Rate** | 1% | Health check |
| **Avg Response Time** | 350ms | Performance baseline |
| **95th Percentile** | 550ms | User experience (slow users) |
| **Min/Max Time** | 100ms / 900ms | Range analysis |
| **Active Threads** | 10 | Load verification |
| **Throughput** | 40 RPS | API capacity |

---

## 🔍 Understanding Feature Files

### Baseline Load Test Feature:

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

**What it does:**
1. **Background:** Sets up 10 concurrent users ramping up over 30 seconds
2. **Scenario:** Makes concurrent GET requests to the API
3. **Then:** Validates responses and records metrics to InfluxDB

---

## 🚀 Run Different Load Scenarios

### Baseline (Light Load - 10 users):
```bash
mvn clean test -Dcucumber.features="src/test/resources/features/baseline_load_test.feature"
```

### Peak Load (Medium - 50 users):
```bash
mvn clean test -Dcucumber.features="src/test/resources/features/peak_load_test.feature"
```

### Both (Full Suite):
```bash
mvn clean test
```

---

## ✅ Verification Checklist

After setup, verify everything works:

- [ ] InfluxDB bucket `jmeter-metrics` created
- [ ] InfluxDB API token generated and stored
- [ ] Environment variables set (INFLUXDB_TOKEN, INFLUXDB_URL, etc.)
- [ ] Grafana data source connected (test passes)
- [ ] Grafana dashboards created (2 dashboards visible)
- [ ] Maven builds successfully (`mvn clean compile`)
- [ ] Tests run without errors (`mvn clean test`)
- [ ] Metrics appear in Grafana dashboards

**If any step fails, check the detailed setup guides:**
- Problem with InfluxDB? → Read [INFLUXDB_SETUP.md](INFLUXDB_SETUP.md)
- Problem with Grafana? → Read [GRAFANA_SETUP.md](GRAFANA_SETUP.md)
- Problem with tests? → Read [README.md](README.md) troubleshooting section

---

## 📚 Full Documentation

| Document | Purpose |
|----------|---------|
| **[README.md](README.md)** | Complete overview, advanced scenarios, CI/CD integration |
| **[INFLUXDB_SETUP.md](INFLUXDB_SETUP.md)** | InfluxDB bucket creation, token generation, security |
| **[GRAFANA_SETUP.md](GRAFANA_SETUP.md)** | Grafana data source setup, dashboard creation, queries |
| **[QUICK_START.md](QUICK_START.md)** | You are here! Quick setup in 5 minutes |

---

## 🔐 Security Notes

⚠️ **Important:** Never commit API tokens to Git!

The `.gitignore` file already excludes:
- `.env` files
- `.token` files
- InfluxDB credentials

To store token safely:
```bash
# Option 1: Environment variable (recommended)
export INFLUXDB_TOKEN="your-token"

# Option 2: Local file (add to .gitignore)
echo "your-token" > .env

# Option 3: CI/CD secrets (GitHub Actions, Jenkins)
# Store in secrets and reference as: ${{ secrets.INFLUXDB_TOKEN }}
```

---

## 🎯 Portfolio Highlights

This project demonstrates:

1. **BDD Testing Expertise**
   - Gherkin syntax (feature files)
   - Step definitions (reusable test code)
   - Scenario outlines (parameterized tests)

2. **Performance Testing**
   - Concurrent load scenarios (10-100+ users)
   - Ramp-up patterns
   - Real API testing against public endpoint
   - Percentile analysis (p95, p99)

3. **Cloud Services Integration**
   - InfluxDB Cloud for time-series metrics
   - Grafana Cloud for visualization
   - API token management and security

4. **CI/CD Ready**
   - Maven build integration
   - Jenkins pipeline examples
   - GitHub Actions workflow examples
   - Test result archiving

5. **Professional Documentation**
   - Feature files (self-documenting)
   - Setup guides for team members
   - Troubleshooting section
   - Real metrics and baselines

---

## 🤔 Common Questions

**Q: Can I add more test scenarios?**  
A: Yes! Create new `.feature` files in `src/test/resources/features/` and add corresponding step definitions.

**Q: How do I test a different API?**  
A: Update the base URL and endpoints in feature files. Step definitions are generic and reusable.

**Q: Can I run tests in parallel?**  
A: Yes! Use Maven Surefire plugin configuration to enable parallel test execution.

**Q: What's the cost?**  
A: Free! All tools used have free tiers:
- Apache Maven: Open source
- Cucumber: Open source
- RestAssured: Open source
- InfluxDB Cloud: 30-day free, no payment needed
- Grafana Cloud: Free tier (1 user, unlimited dashboards)

---

## 🚀 Next Steps

1. ✅ Complete 5-minute setup above
2. ✅ Run your first test and see metrics
3. ⏭️ Build custom dashboards in Grafana
4. ⏭️ Add more endpoints or scenarios
5. ⏭️ Integrate into CI/CD pipeline

---

**Last Updated:** 2026-09-28

For detailed setup, see [README.md](README.md)
