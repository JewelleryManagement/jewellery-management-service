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

    assertTrue(
        event.isPresent(),
        "Event of type " + eventType + " for entity " + toCompare + " not logged");
  }

  public static Map<String, Object> getUpdateEventPayload(
      Object entityBefore, Object entityAfter, ObjectMapper objectMapper) {
    Map<String, Object> payloadUpdatedEvent = new HashMap<>();
    payloadUpdatedEvent.put(
        "entityBefore", objectMapper.convertValue(entityBefore, new TypeReference<>() {}));
    payloadUpdatedEvent.put(
        "entityAfter", objectMapper.convertValue(entityAfter, new TypeReference<>() {}));
    return payloadUpdatedEvent;
  }

  public static Map<String, Object> getCreateOrDeleteEventPayload(
      Object entity, ObjectMapper objectMapper) {
    return Map.of("entity", objectMapper.convertValue(entity, new TypeReference<>() {}));
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
}
