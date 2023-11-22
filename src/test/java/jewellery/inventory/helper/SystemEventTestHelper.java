package jewellery.inventory.helper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

public class SystemEventTestHelper {

  public static void assertEventWasLogged(
      TestRestTemplate testRestTemplate,
      ObjectMapper objectMapper,
      String baseSystemEventUrl,
      EventType eventType,
      Map<String, Object> toCompare)
      throws JsonProcessingException {

    Optional<SystemEvent> event =
        findEventByTypeAndEntity(testRestTemplate, objectMapper, baseSystemEventUrl, eventType, toCompare);

    assertTrue(event.isPresent(), "Event of type " + eventType + " for entity " + " not logged");
  }

  private static Optional<SystemEvent> findEventByTypeAndEntity(
      TestRestTemplate testRestTemplate,
      ObjectMapper objectMapper,
      String baseSystemEventUrl,
      EventType eventType,
      Map<String, Object> toCompare)
      throws JsonProcessingException {

    List<SystemEvent> events =
        fetchAllSystemEvents(testRestTemplate, objectMapper, baseSystemEventUrl);
    return events.stream()
        .filter(
            event -> event.getType().equals(eventType) && isSubmap(toCompare, event.getPayload()))
        .findFirst();
  }

  private static List<SystemEvent> fetchAllSystemEvents(
      TestRestTemplate testRestTemplate, ObjectMapper objectMapper, String baseSystemEventUrl)
      throws JsonProcessingException {

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(baseSystemEventUrl, String.class);
    return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
  }

  public static boolean isSubmap(Map<String, Object> submap, Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : submap.entrySet()) {
      String submapKey = entry.getKey();
      Object submapValue = entry.getValue();

      boolean endValueNotEqualsValue =
          !(submapValue instanceof Map<?, ?>) && !Objects.equals(submapValue, map.get(submapKey));
      if (!map.containsKey(submapKey) || endValueNotEqualsValue) {
        return false;
      }

      if (submapValue instanceof Map
          && !isSubmap(
              (Map<String, Object>) submapValue, (Map<String, Object>) map.get(submapKey))) {
        return false;
      }
    }

    return true;
  }

  public static Map<String, Object> getEntityAsMap(Object entity, ObjectMapper objectMapper) {
    return objectMapper.convertValue(entity, new TypeReference<>() {});
  }

  public static Map<String, Object> getUpdateEventPayload(
      Map<String, Object> entityBefore, Map<String, Object> entityAfter) {
    Map<String, Object> payloadUpdatedEvent = new HashMap<>();
    payloadUpdatedEvent.put("entityBefore", entityBefore);
    payloadUpdatedEvent.put("entityAfter", entityAfter);
    return payloadUpdatedEvent;
  }

  public static Map<String, Object> getCreateOrDeleteEventPayload(Map<String, Object> entity) {
    return Map.ofEntries(createEntity(entity));
  }

  public static Map.Entry<String, Map<String, Object>> createEntityBefore(
      Map<String, Object> value) {
    return Map.entry("entityBefore", value);
  }

  public static Map.Entry<String, Map<String, Object>> createEntityAfter(
      Map<String, Object> value) {
    return Map.entry("entityAfter", value);
  }

  public static Map.Entry<String, Map<String, Object>> createEntity(Map<String, Object> value) {
    return Map.entry("entity", value);
  }
}
