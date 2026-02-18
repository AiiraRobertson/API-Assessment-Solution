package utils;

import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;

public class ResponseValidator {

    /**
     * Assert the response status code matches expected
     */
    public static void assertStatusCode(Response response, int expectedCode) {
        Assert.assertEquals(response.getStatusCode(), expectedCode,
                "Expected status " + expectedCode + " but got " + response.getStatusCode());
    }

    /**
     * Assert the response time is within the given threshold (milliseconds)
     */
    public static void assertResponseTimeBelow(Response response, long thresholdMs) {
        long actualTime = response.getTime();
        Assert.assertTrue(actualTime < thresholdMs,
                "Response time " + actualTime + "ms exceeded threshold of " + thresholdMs + "ms");
    }

    /**
     * Assert a specific JSON field is present and not null
     */
    public static void assertFieldNotNull(Response response, String fieldPath) {
        Object value = response.jsonPath().get(fieldPath);
        Assert.assertNotNull(value, "Field '" + fieldPath + "' should not be null");
    }

    /**
     * Assert the response body contains a non-empty JSON array
     */
    public static void assertNonEmptyList(Response response) {
        List<?> items = response.jsonPath().getList("$");
        Assert.assertFalse(items.isEmpty(), "Response list should not be empty");
    }

    /**
     * Assert Content-Type header contains expected value
     */
    public static void assertContentType(Response response, String expectedType) {
        String contentType = response.getContentType();
        Assert.assertTrue(contentType.contains(expectedType),
                "Expected Content-Type containing '" + expectedType + "' but got '" + contentType + "'");
    }

    /**
     * Assert a JSON field equals the expected value
     */
    public static void assertFieldEquals(Response response, String fieldPath, Object expectedValue) {
        Object actual = response.jsonPath().get(fieldPath);
        Assert.assertEquals(actual, expectedValue,
                "Field '" + fieldPath + "' expected '" + expectedValue + "' but got '" + actual + "'");
    }
}
