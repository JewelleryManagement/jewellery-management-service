package jewellery.inventory.helper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemEventTestHelper {

  private final TestRestTemplate testRestTemplate;
  private final ObjectMapper objectMapper;

  public void assertEventWasLogged(EventType eventType, Map<String, Object> toCompare)
      throws JsonProcessingException {

    Optional<SystemEvent> event =
        findEventByTypeAndEntity(testRestTemplate, objectMapper, eventType, toCompare);

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
      EventType eventType,
      Map<String, Object> toCompare)
      throws JsonProcessingException {

    List<SystemEvent> events = fetchAllSystemEvents(testRestTemplate, objectMapper);
    return events.stream()
        .filter(
            event ->
                event.getType().equals(eventType) && Objects.equals(toCompare, event.getPayload()))
        .findFirst();
  }

  private static List<SystemEvent> fetchAllSystemEvents(
      TestRestTemplate testRestTemplate, ObjectMapper objectMapper) throws JsonProcessingException {

    ResponseEntity<String> response = testRestTemplate.getForEntity("/system-events", String.class);
    return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
  }

  @PostConstruct
  public void configureObjectMapper() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(BigDecimal.class, new ToStringSerializer());
    module.addDeserializer(
        BigDecimal.class,
        new StdScalarDeserializer<>(BigDecimal.class) {
          @Override
          public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt)
              throws IOException {
            return new BigDecimal(p.getValueAsString());
          }
        });

    objectMapper.registerModule(module);
  }
}
