package services;

import data.RequestDataFactory;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ActivityService {

    private final RequestDataFactory dataFactory = new RequestDataFactory();
    private static final Logger log = LogManager.getLogger(ActivityService.class);

    private static final String ACTIVITIES_ENDPOINT = "/api/v1/Activities";

    /**
     * POST - Create a new activity with default data
     */
    public Response createActivity() {
        Map<String, Object> payload = dataFactory.buildNewActivityPayload();
        log.info("POST {} - Creating activity with payload: {}", ACTIVITIES_ENDPOINT, payload);

        Response response = given()
                .header("Content-Type", "application/json")
                .body(payload)
                .when()
                .post(ACTIVITIES_ENDPOINT)
                .then()
                .extract()
                .response();

        log.info("POST {} - Status: {}", ACTIVITIES_ENDPOINT, response.getStatusCode());
        return response;
    }

    /**
     * POST - Create a new activity with custom data
     */
    public Response createActivity(Map<String, Object> customPayload) {
        log.info("POST {} - Creating activity with custom payload: {}", ACTIVITIES_ENDPOINT, customPayload);

        Response response = given()
                .header("Content-Type", "application/json")
                .body(customPayload)
                .when()
                .post(ACTIVITIES_ENDPOINT)
                .then()
                .extract()
                .response();

        log.info("POST {} - Status: {}", ACTIVITIES_ENDPOINT, response.getStatusCode());
        return response;
    }

    /**
     * GET - Fetch a single activity by its ID
     */
    public Response fetchActivityById(int activityId) {
        String endpoint = ACTIVITIES_ENDPOINT + "/" + activityId;
        log.info("GET {} - Fetching activity", endpoint);

        Response response = given()
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        log.info("GET {} - Status: {}", endpoint, response.getStatusCode());
        log.debug("GET {} - Body: {}", endpoint, response.asString());
        return response;
    }

    /**
     * GET - Fetch all activities
     */
    public Response fetchAllActivities() {
        log.info("GET {} - Fetching all activities", ACTIVITIES_ENDPOINT);

        Response response = given()
                .when()
                .get(ACTIVITIES_ENDPOINT)
                .then()
                .extract()
                .response();

        log.info("GET {} - Status: {} | Count: {}", ACTIVITIES_ENDPOINT,
                response.getStatusCode(), response.jsonPath().getList("$").size());
        return response;
    }

    /**
     * PUT - Update an existing activity
     */
    public Response updateActivity(int activityId, Map<String, Object> updatedPayload) {
        String endpoint = ACTIVITIES_ENDPOINT + "/" + activityId;
        log.info("PUT {} - Updating activity with: {}", endpoint, updatedPayload);

        Response response = given()
                .header("Content-Type", "application/json")
                .body(updatedPayload)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();

        log.info("PUT {} - Status: {}", endpoint, response.getStatusCode());
        return response;
    }

    /**
     * DELETE - Remove an activity by ID
     */
    public Response deleteActivity(int activityId) {
        String endpoint = ACTIVITIES_ENDPOINT + "/" + activityId;
        log.info("DELETE {} - Removing activity", endpoint);

        Response response = given()
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();

        log.info("DELETE {} - Status: {}", endpoint, response.getStatusCode());
        return response;
    }
}
