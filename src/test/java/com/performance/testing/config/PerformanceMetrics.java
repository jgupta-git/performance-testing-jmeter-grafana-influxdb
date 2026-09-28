package com.performance.testing.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.*;

public class PerformanceMetrics {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetrics.class);

    private InfluxDBClient influxDBClient;
    private WriteApi writeApi;
    private String bucket;
    private String org;
    private long totalRequests;
    private int errorCount;
    private long averageResponseTime;
    private long percentile95;
    private long minResponseTime = Long.MAX_VALUE;
    private long maxResponseTime = 0;

    public PerformanceMetrics() {
        initInfluxDB();
    }

    private void initInfluxDB() {
        try {
            String url = System.getenv("INFLUXDB_URL");
            String token = System.getenv("INFLUXDB_TOKEN");
            this.org = System.getenv("INFLUXDB_ORG");
            this.bucket = System.getenv("INFLUXDB_BUCKET");

            if (url == null || token == null) {
                logger.warn("InfluxDB credentials not configured. Metrics will not be sent to InfluxDB.");
                return;
            }

            this.influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            this.writeApi = influxDBClient.getWriteApi(com.influxdb.client.domain.WritePrecision.MS);
            logger.info("Connected to InfluxDB: " + url);
        } catch (Exception e) {
            logger.error("Failed to initialize InfluxDB client", e);
        }
    }

    public void recordMetric(String testName, List<Response> responses, List<Long> responseTimes, int errors, int threadCount) {
        if (influxDBClient == null) {
            logger.warn("InfluxDB not configured. Skipping metric recording.");
            return;
        }

        try {
            Collections.sort(responseTimes);

            long avgTime = (long) responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0);

            int p95Index = (int) (0.95 * responseTimes.size());
            long p95Time = responseTimes.get(Math.min(p95Index, responseTimes.size() - 1));

            long minTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .min()
                    .orElse(0);

            long maxTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0);

            double errorRate = (errors * 100.0) / responses.size();

            Point point = Point.measurement("performance")
                    .addTag("test_name", testName)
                    .addTag("environment", "cucumberbdd")
                    .addField("total_requests", responses.size())
                    .addField("error_count", errors)
                    .addField("error_rate", errorRate)
                    .addField("avg_response_time", avgTime)
                    .addField("min_response_time", minTime)
                    .addField("max_response_time", maxTime)
                    .addField("p95_response_time", p95Time)
                    .addField("active_threads", threadCount)
                    .addField("throughput", responses.size() / 300.0)  // Assuming 5-minute test
                    .time(Instant.now(), WritePrecision.MS);

            writeApi.writePoint(bucket, org, point);
            logger.info("Metric recorded: " + testName + " - Samples: " + responses.size() + ", Errors: " + errors);
        } catch (Exception e) {
            logger.error("Failed to record metric: " + e.getMessage(), e);
        }
    }

    public void sendToInfluxDB(String metricName, Map<String, Object> fields, Map<String, String> tags) {
        if (influxDBClient == null) {
            return;
        }

        try {
            Point.Builder pointBuilder = Point.measurement(metricName)
                    .time(Instant.now(), WritePrecision.MS);

            tags.forEach(pointBuilder::addTag);
            fields.forEach(pointBuilder::addField);

            writeApi.writePoint(bucket, org, pointBuilder.build());
            logger.debug("Sent metric to InfluxDB: " + metricName);
        } catch (Exception e) {
            logger.error("Failed to send metric to InfluxDB", e);
        }
    }

    public void close() {
        if (influxDBClient != null) {
            try {
                influxDBClient.close();
                logger.info("InfluxDB client closed");
            } catch (Exception e) {
                logger.error("Error closing InfluxDB client", e);
            }
        }
    }

    // Getters and Setters
    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public long getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(long averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public long getPercentile95() {
        return percentile95;
    }

    public void setPercentile95(long percentile95) {
        this.percentile95 = percentile95;
    }

    public long getMinResponseTime() {
        return minResponseTime;
    }

    public void setMinResponseTime(long minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(long maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }
}
