package com.performance.testing.steps;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import com.performance.testing.config.PerformanceMetrics;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import static io.restassured.RestAssured.*;

public class ApiPerformanceSteps {

    private String baseUrl;
    private int userCount;
    private int rampUpTime;
    private List<Response> responses;
    private List<Long> responseTimes;
    private long testStartTime;
    private long testEndTime;
    private int errorCount;
    private Scenario scenario;
    private PerformanceMetrics metrics;

    @Before
    public void setUp(Scenario scenario) {
        this.scenario = scenario;
        this.responses = Collections.synchronizedList(new ArrayList<>());
        this.responseTimes = Collections.synchronizedList(new ArrayList<>());
        this.errorCount = 0;
        this.metrics = new PerformanceMetrics();
    }

    @Given("base URL is {string}")
    public void setBaseUrl(String url) {
        this.baseUrl = url;
        baseURI = url;
    }

    @Given("users count is {int}")
    public void setUserCount(int count) {
        this.userCount = count;
    }

    @Given("ramp-up time is {int} seconds")
    public void setRampUpTime(int seconds) {
        this.rampUpTime = seconds;
    }

    @When("user makes concurrent GET requests to {string}")
    public void makeConcurrentGetRequests(String endpoint) {
        makeConcurrentRequests(endpoint, "GET", null);
    }

    @When("user makes concurrent POST requests to {string} with body")
    public void makeConcurrentPostRequests(String endpoint, Map<String, String> data) {
        makeConcurrentRequests(endpoint, "POST", data);
    }

    @When("user makes concurrent PUT requests to {string} with body")
    public void makeConcurrentPutRequests(String endpoint, Map<String, String> data) {
        makeConcurrentRequests(endpoint, "PUT", data);
    }

    private void makeConcurrentRequests(String endpoint, String method, Map<String, String> body) {
        testStartTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(userCount);
        CountDownLatch latch = new CountDownLatch(userCount);
        long delayBetweenThreads = (rampUpTime * 1000) / userCount;

        for (int i = 0; i < userCount; i++) {
            final int threadId = i;
            executor.execute(() -> {
                try {
                    Thread.sleep(threadId * delayBetweenThreads);
                    long startTime = System.currentTimeMillis();

                    Response response = executeRequest(endpoint, method, body);
                    responses.add(response);

                    long responseTime = System.currentTimeMillis() - startTime;
                    responseTimes.add(responseTime);

                    if (response.getStatusCode() >= 400) {
                        errorCount++;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        testEndTime = System.currentTimeMillis();
        calculateMetrics();
    }

    private Response executeRequest(String endpoint, String method, Map<String, String> body) {
        switch (method.toUpperCase()) {
            case "GET":
                return given().when().get(endpoint);
            case "POST":
                return given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post(endpoint);
            case "PUT":
                return given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .put(endpoint);
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    @Then("all responses should have status code {int}")
    public void validateStatusCode(int expectedCode) {
        List<Integer> failedCodes = responses.stream()
                .map(Response::getStatusCode)
                .filter(code -> code != expectedCode)
                .collect(Collectors.toList());

        if (!failedCodes.isEmpty()) {
            scenario.attach("Failed status codes: " + failedCodes, "text/plain", "status_codes");
        }

        assert failedCodes.isEmpty() : "Expected status code " + expectedCode + " but got " + failedCodes;
    }

    @Then("response time should be less than {int} ms")
    public void validateResponseTime(int maxTime) {
        long avgResponseTime = (long) responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        String message = "[RESULT] Average Response Time: " + avgResponseTime + "ms (Target: " + maxTime + "ms)";
        scenario.attach(message, "text/plain", "response_time_avg");

        assert avgResponseTime <= maxTime : "Average response time " + avgResponseTime + "ms exceeds target " + maxTime + "ms";
    }

    @Then("{int}th percentile response time should be less than {int} ms")
    public void validatePercentileResponseTime(int percentile, int maxTime) {
        Collections.sort(responseTimes);
        int index = (int) ((percentile / 100.0) * responseTimes.size());
        long percentileTime = responseTimes.get(Math.min(index, responseTimes.size() - 1));

        String message = "[RESULT] " + percentile + "th Percentile Response Time: " + percentileTime + "ms (Target: " + maxTime + "ms)";
        scenario.attach(message, "text/plain", "response_time_percentile");

        assert percentileTime <= maxTime : percentile + "th percentile " + percentileTime + "ms exceeds target " + maxTime + "ms";
    }

    @Then("throughput should be greater than {int} RPS")
    public void validateThroughput(int minRps) {
        long duration = testEndTime - testStartTime;
        double rps = (responses.size() / (duration / 1000.0));

        String message = "[RESULT] Throughput: " + String.format("%.2f", rps) + " RPS (Target: > " + minRps + " RPS)";
        scenario.attach(message, "text/plain", "throughput");

        assert rps >= minRps : "Throughput " + rps + " RPS is below target " + minRps + " RPS";
    }

    @Then("error rate should be less than {int}%")
    public void validateErrorRate(int maxErrorPercent) {
        double errorPercent = (errorCount * 100.0) / responses.size();

        String message = "[RESULT] Error Rate: " + String.format("%.2f", errorPercent) + "% (Target: < " + maxErrorPercent + "%)";
        scenario.attach(message, "text/plain", "error_rate");

        assert errorPercent < maxErrorPercent : "Error rate " + errorPercent + "% exceeds target " + maxErrorPercent + "%";
    }

    @Then("active threads count should match {int}")
    public void validateActiveThreads(int expectedThreads) {
        assert userCount == expectedThreads : "Expected " + expectedThreads + " threads but got " + userCount;
    }

    @Then("average response time should be less than {int} ms")
    public void validateAverageResponseTime(int maxTime) {
        long avgTime = (long) responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        String message = "[RESULT] Average Response Time: " + avgTime + "ms (Target: " + maxTime + "ms)";
        scenario.attach(message, "text/plain", "avg_response_time");

        assert avgTime <= maxTime : "Average response time exceeds target";
    }

    @Then("system should sustain {int} concurrent users")
    public void validateSustainedLoad(int users) {
        int errorThreshold = (int) ((errorCount * 100.0) / responses.size());
        assert errorThreshold < 5 : "System cannot sustain " + users + " concurrent users";
    }

    @Then("record metric {string}")
    public void recordMetric(String metricName) {
        metrics.recordMetric(metricName, responses, responseTimes, errorCount, userCount);
        String message = "[METRIC] " + metricName + " - Samples: " + responses.size() + ", Errors: " + errorCount;
        scenario.attach(message, "text/plain", metricName.replace(" ", "_"));
    }

    private void calculateMetrics() {
        metrics.setTotalRequests(responses.size());
        metrics.setErrorCount(errorCount);
        metrics.setAverageResponseTime((long) responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0));

        Collections.sort(responseTimes);
        int p95Index = (int) (0.95 * responseTimes.size());
        metrics.setPercentile95(responseTimes.get(Math.min(p95Index, responseTimes.size() - 1)));
    }

    @After
    public void tearDown() {
        String summary = "\n=== Performance Test Summary ===\n" +
                "Total Requests: " + responses.size() + "\n" +
                "Errors: " + errorCount + "\n" +
                "Avg Response Time: " + metrics.getAverageResponseTime() + "ms\n" +
                "95th Percentile: " + metrics.getPercentile95() + "ms\n" +
                "Test Duration: " + (testEndTime - testStartTime) + "ms\n";
        scenario.attach(summary, "text/plain", "test_summary");
    }
}
