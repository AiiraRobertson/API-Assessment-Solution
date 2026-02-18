package tests.functional;

import base.BaseSetup;
import data.RequestDataFactory;
import io.restassured.response.Response;
import services.ActivityService;
import utils.ResponseValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class FunctionalTestSuite extends BaseSetup {

    private ActivityService activityService;
    private RequestDataFactory dataFactory;

    @BeforeClass
    public void setupService() {
        super.configureRestAssured();
        activityService = new ActivityService();
        dataFactory = new RequestDataFactory();
    }

    @DataProvider(name = "validActivityIds")
    public Object[][] validActivityIds() {
        return new Object[][]{{1}, {5}, {10}, {20}, {30}};
    }

    @Test(groups = "functional", priority = 1)
    public void testCreateActivityWithValidData() {
        report = reportManager.createTest("Functional: Create Activity - Valid Data");
        report.info("Creating an activity with all required fields");

        Map<String, Object> payload = dataFactory.buildActivityPayload(0, "Functional Test Task", false);
        Response response = activityService.createActivity(payload);

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldEquals(response, "title", "Functional Test Task");
        ResponseValidator.assertFieldEquals(response, "completed", false);
        ResponseValidator.assertFieldNotNull(response, "id");
        ResponseValidator.assertFieldNotNull(response, "dueDate");

        report.pass("Activity created successfully with all fields correctly returned");
    }

    @Test(groups = "functional", priority = 2, dataProvider = "validActivityIds")
    public void testFetchActivityByValidId(int activityId) {
        report = reportManager.createTest("Functional: Get Activity by ID=" + activityId);
        report.info("Fetching activity with ID: " + activityId);

        Response response = activityService.fetchActivityById(activityId);

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldEquals(response, "id", activityId);
        ResponseValidator.assertFieldNotNull(response, "title");
        ResponseValidator.assertFieldNotNull(response, "dueDate");

        report.pass("Activity retrieved correctly for ID=" + activityId);
    }

    @Test(groups = "functional", priority = 3)
    public void testFetchActivityWithInvalidId() {
        report = reportManager.createTest("Functional: Get Activity - Invalid ID");
        report.info("Attempting to fetch activity with non-existent ID 99999");

        Response response = activityService.fetchActivityById(99999);

        ResponseValidator.assertStatusCode(response, 404);

        report.pass("API correctly returns 404 for non-existent activity");
    }

    @Test(groups = "functional", priority = 4)
    public void testUpdateActivityWithValidData() {
        report = reportManager.createTest("Functional: Update Activity");
        report.info("Updating activity ID=5 with new title");

        Map<String, Object> updatePayload = dataFactory.buildUpdatePayload(5, "Renamed Task");
        Response response = activityService.updateActivity(5, updatePayload);

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldEquals(response, "title", "Renamed Task");
        ResponseValidator.assertFieldEquals(response, "id", 5);
        ResponseValidator.assertFieldEquals(response, "completed", true);

        report.pass("Activity updated successfully with correct response data");
    }

    @Test(groups = "functional", priority = 5)
    public void testDeleteActivityReturnsSuccess() {
        report = reportManager.createTest("Functional: Delete Activity");
        report.info("Deleting activity with ID=10");

        Response response = activityService.deleteActivity(10);

        ResponseValidator.assertStatusCode(response, 200);

        report.pass("Delete operation returned 200 OK");
    }

    @Test(groups = "functional", priority = 6)
    public void testCreateActivityWithMinimalPayload() {
        report = reportManager.createTest("Functional: Create Activity - Minimal Payload");
        report.info("Creating activity with only the title field");

        Map<String, Object> minimalPayload = dataFactory.buildMinimalPayload();
        Response response = activityService.createActivity(minimalPayload);

        // API may accept or reject minimal payloads — we verify it handles gracefully
        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 200 || statusCode == 400,
                "Expected 200 or 400 but got " + statusCode);

        report.pass("API handled minimal payload gracefully with status: " + statusCode);
    }

    @Test(groups = "functional", priority = 7)
    public void testResponseContainsCorrectContentType() {
        report = reportManager.createTest("Functional: Verify Content-Type Header");
        report.info("Checking that response includes application/json content type");

        Response response = activityService.fetchAllActivities();

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertContentType(response, "application/json");

        report.pass("Content-Type header correctly set to application/json");
    }

    @Test(groups = "functional", priority = 8)
    public void testActivityResponseStructure() {
        report = reportManager.createTest("Functional: Validate Response Body Structure");
        report.info("Verifying that activity response contains all expected fields");

        Response response = activityService.fetchActivityById(3);

        ResponseValidator.assertStatusCode(response, 200);
        ResponseValidator.assertFieldNotNull(response, "id");
        ResponseValidator.assertFieldNotNull(response, "title");
        ResponseValidator.assertFieldNotNull(response, "dueDate");
        
        Assert.assertNotNull(response.jsonPath().get("completed"),
                "Field 'completed' should be present in the response");

        report.pass("Response body contains all expected fields: id, title, dueDate, completed");
    }

    @Test(groups = "functional", priority = 9)
    public void testFetchAllActivitiesReturnsList() {
        report = reportManager.createTest("Functional: All Activities Returns Array");
        report.info("Verifying the all-activities endpoint returns a JSON array");

        Response response = activityService.fetchAllActivities();

        ResponseValidator.assertStatusCode(response, 200);
        int count = response.jsonPath().getList("$").size();
        Assert.assertTrue(count > 0, "Expected at least one activity but got " + count);

        report.pass("All activities endpoint returns a list with " + count + " items");
    }
}
