package tests.regression;

import base.BaseSetup;
import data.RequestDataFactory;
import io.restassured.response.Response;
import services.ActivityService;
import utils.ResponseValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Regression Test Suite
 *
 * Ensures that existing functionality remains intact when changes are introduced.
 */
public class RegressionTestSuite extends BaseSetup {

    private ActivityService activityService;
    private RequestDataFactory dataFactory;

    @BeforeClass
    public void setupService() {
        super.configureRestAssured();
        activityService = new ActivityService();
        dataFactory = new RequestDataFactory();
    }

    @Test(groups = "regression", priority = 1)
    public void regressionCreateActivity() {
        report = reportManager.createTest("Regression: Create Activity Still Works");
        report.info("Re-validating that the POST create endpoint functions as expected");

        Map<String, Object> payload = dataFactory.buildActivityPayload(0, "Regression Task", false);
        Response response = activityService.createActivity(payload);

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldEquals(response, "title", "Regression Task");
        ResponseValidator.assertFieldNotNull(response, "id");
        ResponseValidator.assertFieldNotNull(response, "dueDate");

        report.pass("Create activity endpoint is functioning correctly");
    }

    @Test(groups = "regression", priority = 2)
    public void regressionFetchSingleActivity() {
        report = reportManager.createTest("Regression: Fetch Single Activity Still Works");
        report.info("Re-validating that the GET single activity endpoint is intact");

        Response response = activityService.fetchActivityById(1);

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldEquals(response, "id", 1);
        ResponseValidator.assertFieldNotNull(response, "title");
        ResponseValidator.assertContentType(response, "application/json");

        report.pass("Single activity fetch is functioning correctly");
    }

    @Test(groups = "regression", priority = 3)
    public void regressionFetchAllActivities() {
        report = reportManager.createTest("Regression: Fetch All Activities Still Works");
        report.info("Re-validating that the GET all activities endpoint returns data");

        Response response = activityService.fetchAllActivities();

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertNonEmptyList(response);
        ResponseValidator.assertContentType(response, "application/json");

        report.pass("All activities endpoint is functioning correctly");
    }

    @Test(groups = "regression", priority = 4)
    public void regressionUpdateActivity() {
        report = reportManager.createTest("Regression: Update Activity Still Works");
        report.info("Re-validating that the PUT update endpoint is intact");

        Map<String, Object> updatePayload = dataFactory.buildUpdatePayload(2, "Regression Updated");
        Response response = activityService.updateActivity(2, updatePayload);

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldEquals(response, "title", "Regression Updated");

        report.pass("Update activity endpoint is functioning correctly");
    }

    @Test(groups = "regression", priority = 5)
    public void regressionDeleteActivity() {
        report = reportManager.createTest("Regression: Delete Activity Still Works");
        report.info("Re-validating that the DELETE endpoint is intact");

        Response response = activityService.deleteActivity(5);

        ResponseValidator.assertStatusCode(response, 200);

        report.pass("Delete activity endpoint is functioning correctly");
    }

    @Test(groups = "regression", priority = 6)
    public void regressionInvalidIdStillReturns404() {
        report = reportManager.createTest("Regression: Invalid ID Still Returns 404");
        report.info("Re-validating that requesting a non-existent ID returns 404");

        Response response = activityService.fetchActivityById(99999);

        ResponseValidator.assertStatusCode(response, 404);

        report.pass("Invalid ID correctly returns 404 - error handling is intact");
    }

    @Test(groups = "regression", priority = 7)
    public void regressionResponseStructureUnchanged() {
        report = reportManager.createTest("Regression: Response Structure Unchanged");
        report.info("Verifying that the activity response body still contains all expected fields");

        Response response = activityService.fetchActivityById(3);

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldNotNull(response, "id");
        ResponseValidator.assertFieldNotNull(response, "title");
        ResponseValidator.assertFieldNotNull(response, "dueDate");
        Assert.assertNotNull(response.jsonPath().get("completed"),
                "Field 'completed' must still exist in the response");

        report.pass("Response structure is unchanged - all expected fields are present");
    }

    // These tests demonstrate what happens when a "code change" alters behavior.
    // In a real scenario, if a developer changed the API response format or
    // modified default values, these tests would catch the regression.

    @Test(groups = "regression", priority = 8)
    public void simulatedChangeVerifyTitleFieldExists() {
        report = reportManager.createTest("Regression Simulation: Verify 'title' Field Still Exists");
        report.info("Simulating a scenario where a code change might rename 'title' to 'name'. "
                + "This test ensures the original 'title' field is still present.");

        Response response = activityService.fetchActivityById(1);
        ResponseValidator.assertStatusCode(response, 200);

        // If someone renamed "title" to "name" in the API, this assertion would catch it
        String titleValue = response.jsonPath().getString("title");
        Assert.assertNotNull(titleValue,
                "REGRESSION DETECTED: 'title' field is missing — possible field rename occurred");

        report.pass("'title' field is present and unchanged — no regression detected");
    }

    @Test(groups = "regression", priority = 9)
    public void simulatedChangeVerifyStatusCodeContract() {
        report = reportManager.createTest("Regression Simulation: Verify Status Code Contract");
        report.info("Simulating a scenario where a code change might alter the status code "
                + "for successful creation (e.g., from 200 to 201).");

        Response response = activityService.createActivity();

        // If someone changed the API to return 201 for creation, this would fail
        Assert.assertEquals(response.getStatusCode(), 200,
                "REGRESSION DETECTED: Create endpoint status code changed from 200");

        report.pass("Status code contract is intact — create still returns 200");
    }

    @Test(groups = "regression", priority = 10)
    public void simulatedChangeVerifyDeleteBehavior() {
        report = reportManager.createTest("Regression Simulation: Verify Delete Behavior Unchanged");
        report.info("Simulating a scenario where delete might return 204 No Content instead of 200.");

        Response response = activityService.deleteActivity(3);

        // If someone changed delete to return 204, this catches it
        Assert.assertEquals(response.getStatusCode(), 200,
                "REGRESSION DETECTED: Delete endpoint status code changed from 200");

        report.pass("Delete behavior is unchanged — still returns 200 OK");
    }
}
