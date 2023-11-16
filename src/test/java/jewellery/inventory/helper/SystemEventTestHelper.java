package jewellery.inventory.helper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.model.User;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

public class SystemEventTestHelper {

  private static final ObjectMapper objectMapper = createObjectMapper();
  private static final TestRestTemplate testRestTemplate = new TestRestTemplate();

  private static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    return mapper;
  }

  public static void assertEventWasLogged(
      TestRestTemplate testRestTemplate,
      String baseSystemEventUrl,
      EventType eventType,
      Map<String, Object> toCompare)
      throws JsonProcessingException {

    Optional<SystemEvent> event =
        findEventByTypeAndEntity(testRestTemplate, baseSystemEventUrl, eventType, toCompare);

    assertTrue(event.isPresent(), "Event of type " + eventType + " for entity " + " not logged");
  }

  public static Optional<SystemEvent> findEventByTypeAndEntity(
      TestRestTemplate testRestTemplate,
      String baseSystemEventUrl,
      EventType eventType,
      Map<String, Object> toCompare)
      throws JsonProcessingException {

    List<SystemEvent> events = fetchAllSystemEvents(testRestTemplate, baseSystemEventUrl);
    return events.stream()
        .filter(
            event -> event.getType().equals(eventType) && isSubmap(toCompare, event.getPayload()))
        .findFirst();
  }

  private static List<SystemEvent> fetchAllSystemEvents(
      TestRestTemplate testRestTemplate, String baseSystemEventUrl) throws JsonProcessingException {

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(baseSystemEventUrl, String.class);
    return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
  }

  public static <K, V> boolean isSubmap(Map<K, V> submap, Map<K, V> map) {
    for (Map.Entry<K, V> entry : submap.entrySet()) {
      K submapKey = entry.getKey();
      V submapValue = entry.getValue();

      boolean endValueNotEqualsValue =
          !(submapValue instanceof Map<?, ?>) && !map.get(submapKey).equals(submapValue);
      if (!map.containsKey(submapKey) || endValueNotEqualsValue) {
        return false;
      }

      if (submapValue instanceof Map
          && !isSubmap((Map<K, V>) submapValue, (Map<K, V>) map.get(submapKey))) {
        return false;
      }
    }

    return true;
  }



  public static Map<String, Object> createUserAsMap(User user) {
    return Map.of("name", user.getName(), "email", user.getEmail(), "id", user.getId().toString());
  }

  public static Map<String, Object> getUpdateEventPayload(
      Map<String, Object> entityBefore, Map<String, Object> entityAfter) {
    return Map.ofEntries(createEntityBefore(entityBefore), createEntityAfter(entityAfter));
  }

  public static Map<String, Object> createCreateOrDeleteEvent(Map<String, Object> entity) {
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

  public static Map.Entry getOwnerEntry(User user) {
    return Map.entry("owner", createUserAsMap(user));
  }
}
