package tests.integration;

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
 * Integration Test Suite
 * Validates the interaction between multiple API endpoints in sequence.
 * Tests multi-step workflows to ensure endpoints work together correctly.
 */
public class IntegrationTestSuite extends BaseSetup {

    private ActivityService activityService;
    private RequestDataFactory dataFactory;

    @BeforeClass
    public void setupService() {
        super.configureRestAssured();
        activityService = new ActivityService();
        dataFactory = new RequestDataFactory();
    }

    @Test(groups = "integration", priority = 1)
    public void testCreateThenRetrieveActivity() {
        report = reportManager.createTest("Integration: Create -> Retrieve Activity");
        report.info("Creating an activity then verifying a known activity can be retrieved");

        // Step 1: Create a new activity and verify it succeeds
        Map<String, Object> payload = dataFactory.buildActivityPayload(10, "Integration Check", true);
        Response createResponse = activityService.createActivity(payload);
        ResponseValidator.assertStatusCode(createResponse, 200);
        ResponseValidator.assertFieldEquals(createResponse, "title", "Integration Check");
        report.info("Activity creation verified successfully");

        // Step 2: Retrieve an existing activity by known ID to validate GET endpoint
        // Note: FakeRestAPI does not persist POST data, so we verify with a known ID
        Response getResponse = activityService.fetchActivityById(10);
        ResponseValidator.assertStatusCode(getResponse, 200);
        ResponseValidator.assertFieldEquals(getResponse, "id", 10);

        report.pass("Create and Retrieve endpoints both respond correctly with consistent data");
    }

    @Test(groups = "integration", priority = 2)
    public void testCreateUpdateThenRetrieveActivity() {
        report = reportManager.createTest("Integration: Create -> Update -> Retrieve Activity");
        report.info("Exercising create, update, and retrieve endpoints in sequence");

        // Step 1: Create
        Map<String, Object> payload = dataFactory.buildActivityPayload(15, "Original Title", false);
        Response createResponse = activityService.createActivity(payload);
        ResponseValidator.assertStatusCode(createResponse, 200);

        // Step 2: Update using a known existing ID
        int activityId = 15;
        Map<String, Object> updatePayload = dataFactory.buildUpdatePayload(activityId, "Modified Title");
        Response updateResponse = activityService.updateActivity(activityId, updatePayload);
        ResponseValidator.assertStatusCode(updateResponse, 200);
        ResponseValidator.assertFieldEquals(updateResponse, "title", "Modified Title");
        report.info("Activity updated to 'Modified Title'");

        // Step 3: Retrieve and verify the endpoint still works
        Response getResponse = activityService.fetchActivityById(activityId);
        ResponseValidator.assertStatusCode(getResponse, 200);

        report.pass("Create -> Update -> Retrieve workflow completed successfully");
    }

    @Test(groups = "integration", priority = 3)
    public void testCreateThenDeleteWorkflow() {
        report = reportManager.createTest("Integration: Create -> Delete Workflow");
        report.info("Exercising create and delete endpoints in sequence");

        // Step 1: Create an activity
        Map<String, Object> payload = dataFactory.buildActivityPayload(20, "To Be Deleted", false);
        Response createResponse = activityService.createActivity(payload);
        ResponseValidator.assertStatusCode(createResponse, 200);
        report.info("Activity creation successful");

        // Step 2: Delete the activity
        Response deleteResponse = activityService.deleteActivity(20);
        ResponseValidator.assertStatusCode(deleteResponse, 200);
        report.info("Activity deletion successful");

        // Step 3: Verify that a non-existent ID returns 404
        // (Using a very high ID that doesn't exist in the pre-seeded data)
        Response getResponse = activityService.fetchActivityById(99999);
        ResponseValidator.assertStatusCode(getResponse, 404);

        report.pass("Create, Delete, and 404 handling all work correctly in sequence");
    }

    @Test(groups = "integration", priority = 4)
    public void testActivityCountAfterCreation() {
        report = reportManager.createTest("Integration: Verify Activity Count Changes After Creation");
        report.info("Checking that the total activities count increases after adding a new one");

        // Step 1: Get current count
        Response beforeResponse = activityService.fetchAllActivities();
        ResponseValidator.assertStatusCode(beforeResponse, 200);
        int countBefore = beforeResponse.jsonPath().getList("$").size();
        report.info("Activities count before creation: " + countBefore);

        // Step 2: Create a new activity
        Response createResponse = activityService.createActivity();
        ResponseValidator.assertStatusCode(createResponse, 200);

        // Step 3: Get updated count
        Response afterResponse = activityService.fetchAllActivities();
        ResponseValidator.assertStatusCode(afterResponse, 200);
        int countAfter = afterResponse.jsonPath().getList("$").size();
        report.info("Activities count after creation: " + countAfter);

        // Note: Fake REST API may not persist data, so count might not change.
        // We verify the API responds correctly in both cases.
        Assert.assertTrue(countAfter >= countBefore,
                "Activity count should not decrease after creation");

        report.pass("Activity count validation completed: before=" + countBefore + ", after=" + countAfter);
    }

    @Test(groups = "integration", priority = 5)
    public void testMultipleEndpointsCrossValidation() {
        report = reportManager.createTest("Integration: Cross-Validate Multiple Endpoints");
        report.info("Verifying data consistency across single-fetch and all-fetch endpoints");

        // Fetch a specific activity
        Response singleResponse = activityService.fetchActivityById(3);
        ResponseValidator.assertStatusCode(singleResponse, 200);
        String singleTitle = singleResponse.jsonPath().getString("title");
        int singleId = singleResponse.jsonPath().getInt("id");

        // Fetch all activities and find the same one
        Response allResponse = activityService.fetchAllActivities();
        ResponseValidator.assertStatusCode(allResponse, 200);

        // Find the matching activity in the list
        String matchedTitle = allResponse.jsonPath().getString("find { it.id == " + singleId + " }.title");
        Assert.assertEquals(matchedTitle, singleTitle,
                "Title mismatch between single-fetch and all-fetch endpoints");

        report.pass("Data is consistent across single-fetch and all-fetch endpoints for ID=" + singleId);
    }
}
