package data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class RequestDataFactory {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Build a standard payload for creating a new activity
     */
    public Map<String, Object> buildNewActivityPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", 0);
        payload.put("title", "Sample Task");
        payload.put("dueDate", ZonedDateTime.now().plusDays(7).format(ISO_FORMAT));
        payload.put("completed", false);
        return payload;
    }

    /**
     * Build a payload with a custom title and completion status
     */
    public Map<String, Object> buildActivityPayload(int id, String title, boolean completed) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("title", title);
        payload.put("dueDate", ZonedDateTime.now().plusDays(3).format(ISO_FORMAT));
        payload.put("completed", completed);
        return payload;
    }

    /**
     * Build a payload for updating an existing activity
     */
    public Map<String, Object> buildUpdatePayload(int id, String updatedTitle) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("title", updatedTitle);
        payload.put("dueDate", ZonedDateTime.now().plusDays(14).format(ISO_FORMAT));
        payload.put("completed", true);
        return payload;
    }

    /**
     * Build a minimal payload (missing optional fields) for negative testing
     */
    public Map<String, Object> buildMinimalPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Minimal Task");
        return payload;
    }
}
