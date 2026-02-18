package utils;

import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;

public class ResponseValidator {

    public static void assertStatusCode(Response response, int expectedCode) {
        Assert.assertEquals(response.getStatusCode(), expectedCode,
                "Expected status " + expectedCode + " but got " + response.getStatusCode());
    }

    public static void assertResponseTimeBelow(Response response, long thresholdMs) {
        long actualTime = response.getTime();
        Assert.assertTrue(actualTime < thresholdMs,
                "Response time " + actualTime + "ms exceeded threshold of " + thresholdMs + "ms");
    }

    public static void assertFieldNotNull(Response response, String fieldPath) {
        Object value = response.jsonPath().get(fieldPath);
        Assert.assertNotNull(value, "Field '" + fieldPath + "' should not be null");
    }

    public static void assertNonEmptyList(Response response) {
        List<?> items = response.jsonPath().getList("$");
        Assert.assertFalse(items.isEmpty(), "Response list should not be empty");
    }

    public static void assertContentType(Response response, String expectedType) {
        String contentType = response.getContentType();
        Assert.assertTrue(contentType.contains(expectedType),
                "Expected Content-Type containing '" + expectedType + "' but got '" + contentType + "'");
    }

    public static void assertFieldEquals(Response response, String fieldPath, Object expectedValue) {
        Object actual = response.jsonPath().get(fieldPath);
        Assert.assertEquals(actual, expectedValue,
                "Field '" + fieldPath + "' expected '" + expectedValue + "' but got '" + actual + "'");
    }
}
