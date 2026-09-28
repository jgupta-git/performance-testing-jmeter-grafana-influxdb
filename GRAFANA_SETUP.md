# Grafana Cloud Setup Guide

This guide covers connecting Grafana to InfluxDB and building performance dashboards.

## Account Information

- **Instance URL:** https://cordialmustard1705.grafana.net
- **Free Tier Limits:** Unlimited dashboards, 1 free user, standard features

---

## Step 1: Connect InfluxDB as a Data Source

### Via Grafana Cloud UI:

1. Navigate to **Connections** (left sidebar → Administration → Connections)
2. Search for **InfluxDB** → Select **InfluxDB** (official plugin)
3. Click **Create a new data source**
4. Configure:
   - **Name:** `InfluxDB-JMeter`
   - **URL:** `https://us-east-1-1.aws.cloud2.influxdata.com`
   - **Organization:** `30a6ee34d37ea258`
   - **Token:** `<your-api-token-from-INFLUXDB_SETUP.md>`
   - **Default Bucket:** `jmeter-metrics`
   - **Min Time Interval:** `1s`
   - **Max Data Points:** `1000`
5. Click **Test & Save**

Expected output:
```
✓ Data source connected
✓ InfluxDB version: Cloud
```

---

## Step 2: Verify Data Source Connection

### Test Query in Grafana Explore:

1. Navigate to **Explore** (left sidebar → Explore)
2. Select data source: `InfluxDB-JMeter`
3. Run test query:

```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "jmeter")
  |> group(by: ["samplerName"])
  |> limit(n: 10)
```

Expected result: JMeter metrics appear (after running tests)

---

## Step 3: Create Performance Dashboards

### Dashboard 1: Real-Time Metrics

#### Create New Dashboard:

1. Click **Create** (top left) → **Dashboard**
2. Click **Add a new panel**
3. Configure first panel:

**Panel 1: Response Time Over Time**

- **Data Source:** `InfluxDB-JMeter`
- **Query (Flux):**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "jmeter" and r._field == "responseTime")
  |> group(by: ["samplerName"])
```
- **Visualization:** Time Series
- **Panel Title:** "Response Time by Endpoint"
- **Y-Axis:** "Response Time (ms)"
- **Legend:** Show samplerName

**Panel 2: Request Throughput (RPS)**

- **Query (Flux):**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "jmeter" and r._field == "count")
  |> group(by: ["samplerName"])
  |> aggregateWindow(every: 10s, fn: sum)
  |> map(fn: (r) => ({r with _value: r._value / 10.0}))
```
- **Visualization:** Time Series
- **Panel Title:** "Requests Per Second"
- **Y-Axis:** "RPS"

**Panel 3: Error Rate**

- **Query (Flux):**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "jmeter" and r._field == "errorCount")
  |> group(by: ["samplerName", "testTitle"])
```
- **Visualization:** Time Series
- **Panel Title:** "Error Count Over Time"
- **Y-Axis:** "Errors"

**Panel 4: Active Threads**

- **Query (Flux):**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "jmeter" and r._field == "activeThreads")
  |> group(by: ["samplerName"])
```
- **Visualization:** Time Series
- **Panel Title:** "Active Threads"
- **Y-Axis:** "Thread Count"

### Save Dashboard:

1. Click **Save** (top right)
2. **Dashboard Name:** `JMeter - Real-Time Metrics`
3. **Tags:** `jmeter`, `performance`, `realtime`
4. Click **Save**

---

### Dashboard 2: Performance Summary

#### Create New Dashboard:

1. Click **Create** → **Dashboard**
2. Add panels for aggregated metrics:

**Panel 1: Average Response Time (Stat)**

- **Query (Flux):**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "jmeter" and r._field == "avgResponseTime")
  |> group(by: ["samplerName"])
  |> mean()
```
- **Visualization:** Stat
- **Title:** "Avg Response Time"
- **Units:** milliseconds
- **Thresholds:** Green (0-200ms), Yellow (200-500ms), Red (500ms+)

**Panel 2: 95th Percentile Response Time**

- **Query (Flux):**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "jmeter" and r._field == "responseTime")
  |> quantile(q: 0.95)
```
- **Visualization:** Stat
- **Title:** "95th Percentile Response Time"
- **Units:** milliseconds

**Panel 3: Success Rate**

- **Query (Flux):**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "jmeter")
  |> group(by: ["statut"])
  |> count()
  |> map(fn: (r) => ({r with pct: float(v: r._value) / 100.0 * 100.0}))
```
- **Visualization:** Gauge
- **Title:** "Success Rate (%)"
- **Units:** percent
- **Thresholds:** Green (>95%), Yellow (90-95%), Red (<90%)

**Panel 4: Total Requests**

- **Query (Flux):**
```flux
from(bucket:"jmeter-metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "jmeter" and r._field == "count")
  |> sum()
```
- **Visualization:** Stat
- **Title:** "Total Requests"

### Save Dashboard:

1. Click **Save**
2. **Dashboard Name:** `JMeter - Performance Summary`
3. **Tags:** `jmeter`, `performance`, `summary`

---

## Step 4: Set Up Alerts (Optional)

### Create Alert for High Response Time:

1. Open dashboard → Edit any **Response Time** panel
2. Click **Alert** tab
3. Configure:
   - **Condition:** `responseTime > 1000ms`
   - **Evaluate:** `Every 1m` for `5m`
   - **Notification Channel:** Email (requires setup in Alerting)
4. Save

---

## Step 5: Grafana Dashboard Sharing

### Make Dashboards Public:

1. Open dashboard → Click **Share** (top right)
2. Select **Public dashboards**
3. Click **Create public dashboard URL**
4. Share URL with team

---

## Grafana Query Reference

### Common Metrics to Query:

| Metric | Field Name | Use Case |
|--------|-----------|----------|
| Response Time | `responseTime` | Track API latency |
| Min/Max/Avg | `minResponseTime`, `maxResponseTime`, `avgResponseTime` | Performance analysis |
| Request Count | `count` | Throughput measurement |
| Error Count | `errorCount` | Failure tracking |
| Active Threads | `activeThreads` | Load visualization |
| Response Code | `responseCode` | HTTP status analysis |
| Connect Time | `connect` | Network latency |

---

## Troubleshooting

### Issue: "No data" in Grafana

**Cause:** JMeter hasn't written metrics yet  
**Fix:** Run JMeter test with InfluxDB backend listener

```bash
mvn clean test \
  -Dinfluxdb.url=https://us-east-1-1.aws.cloud2.influxdata.com \
  -Dinfluxdb.token=your-api-token \
  -Dinfluxdb.org=30a6ee34d37ea258 \
  -Dinfluxdb.bucket=jmeter-metrics
```

### Issue: "Connection refused" error

**Cause:** InfluxDB URL or token incorrect  
**Fix:** Verify in Grafana:
1. Go to **Data Sources** → `InfluxDB-JMeter`
2. Check URL: `https://us-east-1-1.aws.cloud2.influxdata.com`
3. Re-test connection

### Issue: "Query syntax error"

**Cause:** Flux query has typos  
**Fix:**
1. Use **Explore** → **Query Assistant** for validation
2. Test simpler queries first:
```flux
from(bucket:"jmeter-metrics") |> range(start: -1h) |> limit(n: 100)
```

---

## Dashboard JSON Export/Import

### Export Dashboard as JSON:

1. Open dashboard → Click **Share** (top right)
2. Select **Export** → **Save JSON**
3. Save to file: `dashboards/jmeter-realtime.json`

### Import Dashboard:

1. Click **Dashboards** (left sidebar) → **New** → **Import**
2. Paste JSON or upload file
3. Select data source: `InfluxDB-JMeter`
4. Click **Import**

---

## Commands Cheat Sheet

```bash
# Get Grafana API Token (for CI/CD automation)
curl -X GET https://cordialmustard1705.grafana.net/api/auth/keys \
  -H "Authorization: Bearer <grafana-user-token>"

# Create Dashboard via API
curl -X POST https://cordialmustard1705.grafana.net/api/dashboards/db \
  -H "Authorization: Bearer <grafana-api-token>" \
  -H "Content-Type: application/json" \
  -d @dashboard-definition.json
```

---

## Next Steps

1. ✅ Connect InfluxDB data source
2. ✅ Verify connection with test query
3. ✅ Create real-time metrics dashboard
4. ✅ Create performance summary dashboard
5. ⏭️ Run JMeter tests and monitor live
6. ⏭️ Set up alerts for SLA violations
7. ⏭️ Export dashboards for sharing

---

**Last Updated:** 2026-09-28
