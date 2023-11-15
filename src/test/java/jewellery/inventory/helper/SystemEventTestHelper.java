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
      String entityPayloadKey,
      String entityKey,
      String entityValue)
      throws JsonProcessingException {

    Optional<SystemEvent> event =
        findEventByTypeAndEntity(
            testRestTemplate,
            baseSystemEventUrl,
            eventType,
            entityPayloadKey,
            entityKey,
            entityValue);

    assertTrue(event.isPresent(), "Event of type " + eventType + " for entity " + " not logged");
  }

  public static Optional<SystemEvent> findEventByTypeAndEntity(
      TestRestTemplate testRestTemplate,
      String baseSystemEventUrl,
      EventType eventType,
      String entityPayloadKey,
      String entityKey,
      String entityValue)
      throws JsonProcessingException {

    List<SystemEvent> events = fetchAllSystemEvents(testRestTemplate, baseSystemEventUrl);
    return events.stream()
        .filter(
            event ->
                event.getType().equals(eventType)
                    && entityMatches(event.getPayload(), entityPayloadKey, entityKey, entityValue))
        .findFirst();
  }

  private static List<SystemEvent> fetchAllSystemEvents(
      TestRestTemplate testRestTemplate, String baseSystemEventUrl) throws JsonProcessingException {

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(baseSystemEventUrl, String.class);
    return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
  }

  private static boolean entityMatches(
      Map<String, Object> payload, String entityPayloadKey, String entityKey, String entityValue) {
    //    if (!payload.containsKey(entityPayloadKey)) {
    //      return false;
    //    }
    //    Object entityObject = payload.get(entityPayloadKey);
    //    if (entityObject instanceof Map) {
    //      @SuppressWarnings("unchecked")
    //      Map<String, Object> entityMap = (Map<String, Object>) entityObject;
    //      return entityValue.equals(String.valueOf(entityMap.get(entityKey)));
    //    }
    //    return false;
    //  }

    if (!payload.containsKey(entityPayloadKey)) {
      return false;
    }

    Object currentObject = payload.get(entityPayloadKey);
    String[] keys = entityKey.split("\\.");

    for (String key : keys) {
      if (currentObject instanceof Map<?, ?> currentMap) {
        if (!currentMap.containsKey(key)) {
          return false;
        }
        currentObject = currentMap.get(key);
      } else if (currentObject instanceof List<?> list && !((List<?>) currentObject).isEmpty()) {
        currentObject = ((Map<?, ?>) list.get(0)).get(key);
      } else {
        return false;
      }
    }

    return entityValue.equals(String.valueOf(currentObject));
  }
}