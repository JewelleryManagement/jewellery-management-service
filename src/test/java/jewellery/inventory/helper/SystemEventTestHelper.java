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
        findEventByTypeAndEntity(
            testRestTemplate, objectMapper, baseSystemEventUrl, eventType, toCompare);

    assertTrue(event.isPresent(), "Event of type " + eventType + " for entity " + toCompare + " not logged");
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
            event ->
                event.getType().equals(eventType) && Objects.equals(toCompare, event.getPayload()))
        .findFirst();
  }

  private static List<SystemEvent> fetchAllSystemEvents(
      TestRestTemplate testRestTemplate, ObjectMapper objectMapper, String baseSystemEventUrl)
      throws JsonProcessingException {

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(baseSystemEventUrl, String.class);
    return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
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

  public static Map.Entry<String, Map<String, Object>> createEntity(Map<String, Object> value) {
    return Map.entry("entity", value);
  }
}
