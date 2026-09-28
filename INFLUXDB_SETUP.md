# InfluxDB Cloud Setup Guide

This guide covers setting up InfluxDB Cloud to receive JMeter performance metrics.

## Account Information

- **Instance URL:** https://us-east-1-1.aws.cloud2.influxdata.com
- **Organization ID:** 30a6ee34d37ea258
- **Free Tier Limits:** 30-day data retention, limited query throughput

---

## Step 1: Create a Bucket for JMeter Metrics

### Via InfluxDB Cloud UI:

1. Navigate to **Buckets** (left sidebar → Data → Buckets)
2. Click **Create Bucket**
3. Configure:
   - **Name:** `jmeter-metrics`
   - **Retention:** 30 days (free tier default)
   - **Description:** "JMeter performance test metrics"
4. Click **Create**

### Via CLI (if installed):

```bash
influx bucket create \
  --name jmeter-metrics \
  --org 30a6ee34d37ea258 \
  --retention 720h
```

---

## Step 2: Generate API Token

### Via InfluxDB Cloud UI:

1. Navigate to **API Tokens** (left sidebar → Admin → API Tokens)
2. Click **Generate API Token**
3. Select **Custom** token type
4. Grant permissions:
   - ✅ **Read:** `jmeter-metrics` bucket
   - ✅ **Write:** `jmeter-metrics` bucket
5. Configure:
   - **Token Name:** `jmeter-performance-testing`
   - **Description:** "Token for JMeter load test metrics"
6. Click **Generate**
7. **⚠️ COPY THE TOKEN** — it only shows once!
   - Save to: `src/test/jmeter/influxdb.token` (use in .gitignore)
   - Format: Long alphanumeric string starting with letters

### Via CLI:

```bash
influx auth create \
  --org 30a6ee34d37ea258 \
  --description "JMeter performance testing" \
  --write-bucket <bucket-id> \
  --read-bucket <bucket-id>
```

---

## Step 3: Store API Token Securely

### Option A: Use Environment Variable (Recommended for CI/CD)

```bash
# On Windows (PowerShell)
$env:INFLUXDB_TOKEN = "your-api-token-here"

# On Linux/Mac
export INFLUXDB_TOKEN="your-api-token-here"
```

### Option B: Store in Local File

Create `src/test/jmeter/influxdb.token`:
```
your-api-token-here
```

Add to `.gitignore`:
```
src/test/jmeter/influxdb.token
```

### Option C: Hardcode in Test Run (Development Only)

```bash
mvn clean test -Dinfluxdb.token="your-api-token-here"
```

---

## Step 4: Verify InfluxDB Connection

### Create a Test Bucket Query:

1. Go to **Explore** (Flux Query Editor)
2. Run this query:
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -24h)
  |> limit(n: 1)
```

3. Expected result: "No data yet" (until you run JMeter tests)

---

## Step 5: InfluxDB Metrics Schema (Written by JMeter)

When JMeter runs with InfluxDB backend listener, it writes metrics in this format:

### Measurement: `jmeter`

**Tags:**
- `testTitle` — Test plan name
- `samplerName` — Request name (GET /api/users, POST /api/users, etc.)
- `statut` — Status (ok/ko)

**Fields:**
- `responseTime` — Response time in milliseconds
- `connect` — Connection time
- `latency` — Latency in ms
- `requestBytes` — Request size in bytes
- `responseBytes` — Response size in bytes
- `count` — Sample count
- `errorCount` — Error count
- `responseCode` — HTTP response code
- `allThreads` — Total threads
- `activeThreads` — Active threads at time of sample
- `minResponseTime` — Min response time (ms)
- `maxResponseTime` — Max response time (ms)
- `avgResponseTime` — Average response time (ms)

**Example Data Point:**
```
jmeter,testTitle="ReqRes Load Test",samplerName="GET /api/users",statut="ok" 
responseTime=245i,connect=120i,latency=125i,count=10i,allThreads=10i,activeThreads=10i
```

---

## Step 6: InfluxDB Data Retention Policy

### Default Retention:
- **Free Tier:** 30 days
- **Paid Tier:** Configurable (up to unlimited)

### Check Current Retention:

```bash
influx bucket list --org 30a6ee34d37ea258
```

Output shows retention period for `jmeter-metrics` bucket.

### Update Retention (if needed):

```bash
influx bucket update \
  --name jmeter-metrics \
  --retention 720h \  # 30 days
  --org 30a6ee34d37ea258
```

---

## Troubleshooting

### Issue: "401 Unauthorized"
- **Cause:** Invalid or expired API token
- **Fix:** Regenerate token via InfluxDB UI and update `.token` file

### Issue: "Bucket not found"
- **Cause:** Bucket name mismatch
- **Fix:** Verify bucket name matches `jmeter-metrics` exactly (case-sensitive)

### Issue: "401: token required"
- **Cause:** Token not passed to JMeter
- **Fix:** Ensure `INFLUXDB_TOKEN` environment variable is set before running tests:
  ```bash
  set INFLUXDB_TOKEN=your-token  # Windows
  export INFLUXDB_TOKEN=your-token  # Linux/Mac
  mvn clean test
  ```

### Issue: "No data in Grafana"
- **Cause:** JMeter test hasn't run yet, or metrics not reaching InfluxDB
- **Fix:** 
  1. Run a test with `-Dinfluxdb.token=<your-token>`
  2. Check InfluxDB query editor for data (Explore → Query)
  3. Verify Grafana data source is connected to correct bucket

---

## Next Steps

1. ✅ Create bucket: `jmeter-metrics`
2. ✅ Generate API token
3. ✅ Store token securely
4. ⏭️ Run JMeter tests with InfluxDB backend listener
5. ⏭️ Connect Grafana to InfluxDB data source
6. ⏭️ Build performance dashboards

---

## Commands Cheat Sheet

```bash
# Create bucket
influx bucket create --name jmeter-metrics --org 30a6ee34d37ea258

# List buckets
influx bucket list --org 30a6ee34d37ea258

# Generate API token (interactive)
influx auth create --org 30a6ee34d37ea258

# List all tokens
influx auth list --org 30a6ee34d37ea258

# Query metrics
influx query 'from(bucket:"jmeter-metrics") |> range(start: -1h)'
```

---

**Last Updated:** 2026-09-28
