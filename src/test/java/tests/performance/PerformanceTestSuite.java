package tests.performance;

import base.BaseSetup;
import io.restassured.response.Response;
import services.ActivityService;
import utils.ResponseValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performance Test Suite
 * Assesses response times, throughput, and API behavior under load.
 * Includes response time benchmarks, concurrent load testing, and stress testing.
 */
public class PerformanceTestSuite extends BaseSetup {

    private ActivityService activityService;

    private static final long RESPONSE_TIME_THRESHOLD_MS = 3000;
    private static final int LOAD_TEST_CONCURRENT_USERS = 10;
    private static final int LOAD_TEST_REQUESTS_PER_USER = 5;
    private static final int STRESS_TEST_MAX_THREADS = 50;

    @BeforeClass
    public void setupService() {
        super.configureRestAssured();
        activityService = new ActivityService();
    }

    // --- Response Time Benchmarks ---

    @Test(groups = "performance", priority = 1)
    public void testGetAllActivitiesResponseTime() {
        report = reportManager.createTest("Performance: GET All Activities - Response Time");
        report.info("Measuring response time for fetching all activities");

        Response response = activityService.fetchAllActivities();
        long responseTime = response.getTime();

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertResponseTimeBelow(response, RESPONSE_TIME_THRESHOLD_MS);

        report.pass("Response time: " + responseTime + "ms (threshold: " + RESPONSE_TIME_THRESHOLD_MS + "ms)");
    }

    @Test(groups = "performance", priority = 2)
    public void testGetSingleActivityResponseTime() {
        report = reportManager.createTest("Performance: GET Single Activity - Response Time");
        report.info("Measuring response time for fetching a single activity");

        Response response = activityService.fetchActivityById(1);
        long responseTime = response.getTime();

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertResponseTimeBelow(response, RESPONSE_TIME_THRESHOLD_MS);

        report.pass("Response time: " + responseTime + "ms (threshold: " + RESPONSE_TIME_THRESHOLD_MS + "ms)");
    }

    @Test(groups = "performance", priority = 3)
    public void testCreateActivityResponseTime() {
        report = reportManager.createTest("Performance: POST Create Activity - Response Time");
        report.info("Measuring response time for creating an activity");

        Response response = activityService.createActivity();
        long responseTime = response.getTime();

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertResponseTimeBelow(response, RESPONSE_TIME_THRESHOLD_MS);

        report.pass("Response time: " + responseTime + "ms (threshold: " + RESPONSE_TIME_THRESHOLD_MS + "ms)");
    }

    // --- Load Testing ---

    @Test(groups = "performance", priority = 4)
    public void testConcurrentLoadOnGetEndpoint() throws InterruptedException, ExecutionException {
        report = reportManager.createTest("Performance: Load Test - Concurrent GET Requests");
        report.info("Sending " + (LOAD_TEST_CONCURRENT_USERS * LOAD_TEST_REQUESTS_PER_USER)
                + " concurrent requests (" + LOAD_TEST_CONCURRENT_USERS + " users x "
                + LOAD_TEST_REQUESTS_PER_USER + " requests each)");

        ExecutorService executor = Executors.newFixedThreadPool(LOAD_TEST_CONCURRENT_USERS);
        List<Future<Long>> futures = new ArrayList<>();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        int totalRequests = LOAD_TEST_CONCURRENT_USERS * LOAD_TEST_REQUESTS_PER_USER;

        for (int i = 0; i < totalRequests; i++) {
            futures.add(executor.submit(() -> {
                try {
                    Response response = activityService.fetchAllActivities();
                    if (response.getStatusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                    return response.getTime();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    return -1L;
                }
            }));
        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        // Calculate statistics
        long totalTime = 0;
        long maxTime = 0;
        long minTime = Long.MAX_VALUE;
        int validResponses = 0;

        for (Future<Long> future : futures) {
            long time = future.get();
            if (time > 0) {
                totalTime += time;
                maxTime = Math.max(maxTime, time);
                minTime = Math.min(minTime, time);
                validResponses++;
            }
        }

        long avgTime = validResponses > 0 ? totalTime / validResponses : 0;
        double successRate = (double) successCount.get() / totalRequests * 100;

        report.info("Results: Total=" + totalRequests + ", Success=" + successCount.get()
                + ", Failed=" + failureCount.get());
        report.info("Response Times: Avg=" + avgTime + "ms, Min=" + minTime + "ms, Max=" + maxTime + "ms");
        report.info("Success Rate: " + String.format("%.1f", successRate) + "%");

        Assert.assertTrue(successRate >= 80.0,
                "Success rate " + successRate + "% is below acceptable threshold of 80%");

        report.pass("Load test completed with " + String.format("%.1f", successRate)
                + "% success rate | Avg response: " + avgTime + "ms");
    }

    // --- Stress Testing ---

    @Test(groups = "performance", priority = 5)
    public void testStressTestToFindBreakingPoint() throws InterruptedException {
        report = reportManager.createTest("Performance: Stress Test - Finding Breaking Point");
        report.info("Gradually increasing concurrent load to find the API breaking point");

        int[] threadCounts = {5, 10, 20, 30, 50};
        int breakingPoint = -1;

        for (int threadCount : threadCounts) {
            if (threadCount > STRESS_TEST_MAX_THREADS) break;

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            AtomicInteger successes = new AtomicInteger(0);
            AtomicInteger failures = new AtomicInteger(0);
            List<Future<Long>> futures = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() -> {
                    try {
                        Response response = activityService.fetchActivityById(1);
                        if (response.getStatusCode() == 200) {
                            successes.incrementAndGet();
                        } else {
                            failures.incrementAndGet();
                        }
                        return response.getTime();
                    } catch (Exception e) {
                        failures.incrementAndGet();
                        return -1L;
                    }
                }));
            }

            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);

            double successRate = (double) successes.get() / threadCount * 100;
            report.info("Threads: " + threadCount + " | Success Rate: "
                    + String.format("%.1f", successRate) + "% | Failures: " + failures.get());

            if (successRate < 70.0) {
                breakingPoint = threadCount;
                report.info("Breaking point detected at " + threadCount + " concurrent threads");
                break;
            }
        }

        if (breakingPoint == -1) {
            report.pass("API handled all load levels up to " + STRESS_TEST_MAX_THREADS
                    + " concurrent threads without breaking");
        } else {
            report.warning("API breaking point observed at approximately " + breakingPoint + " concurrent threads");
        }
    }

    // --- Throughput Measurement ---

    @Test(groups = "performance", priority = 6)
    public void testThroughputMeasurement() {
        report = reportManager.createTest("Performance: Throughput - Requests Per Second");
        report.info("Measuring how many sequential requests the API can handle per second");

        int totalRequests = 20;
        long startTime = System.currentTimeMillis();
        int successCount = 0;

        for (int i = 0; i < totalRequests; i++) {
            Response response = activityService.fetchActivityById(1);
            if (response.getStatusCode() == 200) {
                successCount++;
            }
        }

        long elapsedMs = System.currentTimeMillis() - startTime;
        double requestsPerSecond = (double) totalRequests / elapsedMs * 1000;

        report.info("Completed " + totalRequests + " requests in " + elapsedMs + "ms");
        report.info("Throughput: " + String.format("%.2f", requestsPerSecond) + " requests/second");
        report.info("Success count: " + successCount + "/" + totalRequests);

        Assert.assertTrue(successCount > 0, "At least some requests should succeed");

        report.pass("Throughput measured at " + String.format("%.2f", requestsPerSecond) + " req/s");
    }
}
