package jewellery.inventory.helper;

import static org.junit.jupiter.api.Assertions.*;

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
import java.util.*;
import jewellery.inventory.dto.response.SystemEventLiteResponseDto;
import jewellery.inventory.dto.response.SystemEventResponseDto;
import jewellery.inventory.model.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemEventTestHelper {

  private final TestRestTemplate testRestTemplate;
  private final ObjectMapper objectMapper;

  public void assertEventWasLogged(
      EventType eventType, Map<String, Object> toCompare, UUID relatedId)
      throws JsonProcessingException {

    Optional<SystemEventLiteResponseDto> eventOptional =
        findEventByTypeAndEntity(testRestTemplate, objectMapper, eventType, toCompare, relatedId);

    SystemEventLiteResponseDto event = null;

    if (eventOptional.isPresent()) {
      event = eventOptional.get();
    }

    ResponseEntity<SystemEventResponseDto> eventWithPayload =
        testRestTemplate.getForEntity(
            "/system-events/" + event.getId(), SystemEventResponseDto.class);

    assertTrue(
        eventOptional.isPresent(),
        "Event of type " + eventType + " for entity " + toCompare + " not logged");
    assertNotNull(event);
    assertEquals(Objects.requireNonNull(eventWithPayload.getBody()).getPayload(), toCompare);
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

  private static Optional<SystemEventLiteResponseDto> findEventByTypeAndEntity(
      TestRestTemplate testRestTemplate,
      ObjectMapper objectMapper,
      EventType eventType,
      Map<String, Object> toCompare,
      UUID relatedId)
      throws JsonProcessingException {

    List<SystemEventLiteResponseDto> events =
        fetchAllSystemEvents(testRestTemplate, objectMapper, relatedId);

    return events.stream().filter(event -> event.getType().equals(eventType)).findFirst();
  }

  private static List<SystemEventLiteResponseDto> fetchAllSystemEvents(
      TestRestTemplate testRestTemplate, ObjectMapper objectMapper, UUID relatedId)
      throws JsonProcessingException {

    ResponseEntity<List<SystemEventLiteResponseDto>> response =
        testRestTemplate.exchange(
            "/system-events/related-to/" + relatedId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SystemEventLiteResponseDto>>() {});
    return response.getBody();
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
