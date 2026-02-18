package tests.smoke;

import base.BaseSetup;
import io.restassured.response.Response;
import services.ActivityService;
import utils.ResponseValidator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Smoke Test Suite
 * Quick verification that the most critical API functionalities are operational.
 * These tests are designed to run frequently and provide fast feedback.
 */
public class SmokeTestSuite extends BaseSetup {

    private ActivityService activityService;

    @BeforeClass
    public void setupService() {
        super.configureRestAssured();
        activityService = new ActivityService();
    }

    @Test(groups = "smoke", priority = 1)
    public void verifyApiIsReachable() {
        report = reportManager.createTest("Smoke: API Health Check");
        report.info("Verifying the API base endpoint is reachable");

        Response response = activityService.fetchAllActivities();
        ResponseValidator.assertStatusCode(response, 200);

        report.pass("API is reachable and responding with status 200");
    }

    @Test(groups = "smoke", priority = 2)
    public void verifyCreateActivity() {
        report = reportManager.createTest("Smoke: Create Activity");
        report.info("Verifying that a new activity can be created via POST");

        Response response = activityService.createActivity();
        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldNotNull(response, "id");
        ResponseValidator.assertFieldEquals(response, "title", "Sample Task");

        report.pass("Activity created successfully with correct title");
    }

    @Test(groups = "smoke", priority = 3)
    public void verifyFetchSingleActivity() {
        report = reportManager.createTest("Smoke: Fetch Single Activity");
        report.info("Verifying that a single activity can be retrieved by ID");

        Response response = activityService.fetchActivityById(1);
        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldNotNull(response, "title");
        ResponseValidator.assertFieldEquals(response, "id", 1);

        report.pass("Single activity fetched correctly with matching ID");
    }

    @Test(groups = "smoke", priority = 4)
    public void verifyFetchAllActivities() {
        report = reportManager.createTest("Smoke: Fetch All Activities");
        report.info("Verifying that the activity list endpoint returns data");

        Response response = activityService.fetchAllActivities();
        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertNonEmptyList(response);

        report.pass("All activities retrieved - list is non-empty");
    }

    @Test(groups = "smoke", priority = 5)
    public void verifyUpdateActivity() {
        report = reportManager.createTest("Smoke: Update Activity");
        report.info("Verifying that an activity can be updated via PUT");

        Response response = activityService.updateActivity(1,
                new data.RequestDataFactory().buildUpdatePayload(1, "Updated Task"));
        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldEquals(response, "title", "Updated Task");

        report.pass("Activity updated successfully");
    }

    @Test(groups = "smoke", priority = 6)
    public void verifyDeleteActivity() {
        report = reportManager.createTest("Smoke: Delete Activity");
        report.info("Verifying that an activity can be deleted");

        Response response = activityService.deleteActivity(1);
        ResponseValidator.assertStatusCode(response, 200);

        report.pass("Activity deleted successfully with status 200");
    }
}
