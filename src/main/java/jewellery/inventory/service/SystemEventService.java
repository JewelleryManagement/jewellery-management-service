package jewellery.inventory.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import jewellery.inventory.dto.response.SystemEventLiteResponseDto;
import jewellery.inventory.dto.response.SystemEventResponseDto;
import jewellery.inventory.exception.not_found.NotFoundException;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.service.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemEventService {

  private final SystemEventRepository systemEventRepository;
  private final AuthService authService;
  private final ObjectMapper objectMapper;

  public List<SystemEventLiteResponseDto> getAllEvents() {
    return systemEventRepository.findAllWithoutRelatedIds();
  }

  public SystemEventResponseDto getSystemEvent(UUID id) {
    return systemEventRepository
        .findByIdWithoutRelatedIds(id)
        .orElseThrow(() -> new NotFoundException("Event with id: " + id + " is not found!"));
  }

  public List<SystemEventLiteResponseDto> getEventsRelatedTo(UUID id) {
    return systemEventRepository.findByRelatedId(id);
  }

  public <T, U> void logEvent(EventType type, T newEntity, @Nullable U oldEntity) {
    Map<String, Object> payload = new HashMap<>();

    payload.put("entityBefore", createMap(oldEntity));
    payload.put("entityAfter", createMap(newEntity));

    logEvent(type, payload);
  }

  public <T> void logEvent(EventType type, T entity) {
    Map<String, Object> payload = new HashMap<>();

    payload.put("entity", createMap(entity));

    logEvent(type, payload);
  }

  private <U> Object createMap(U entity) {
    objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
    return objectMapper.convertValue(entity, new TypeReference<>() {});
  }

  private void logEvent(EventType type, Map<String, Object> payload) {
    SystemEvent event = new SystemEvent();
    Map<String, Object> executor = getCurrentUser();
    event.setExecutor(executor);
    event.setType(type);
    event.setTimestamp(Instant.now());
    event.setPayload(payload);

    Set<UUID> ids = extractRelatedIds(payload);
    addExecutorId(ids, executor);
    event.setRelatedIds(ids);

    systemEventRepository.save(event);
  }

  private Map<String, Object> getCurrentUser() {
    Object executor = authService.getCurrentUser();
    return executor != null
        ? objectMapper.convertValue(executor, new TypeReference<>() {})
        : Map.of();
  }

  private Set<UUID> extractRelatedIds(Map<String, Object> payload) {
    Set<UUID> relatedIds = new HashSet<>();
    if (payload == null || payload.isEmpty()) return relatedIds;

    JsonNode root = objectMapper.valueToTree(payload);
    collectRelatedIds(root, relatedIds);
    return relatedIds;
  }

  private void collectRelatedIds(JsonNode currentNode, Set<UUID> collectedIds) {
    if (currentNode == null || currentNode.isNull()) return;

    if (currentNode.isObject()) {
      var fieldIterator = currentNode.fields();

      while (fieldIterator.hasNext()) {
        var field = fieldIterator.next();
        String fieldName = field.getKey();
        JsonNode fieldValue = field.getValue();

        if (("id".equals(fieldName) || "contentOf".equals(fieldName))
            && fieldValue != null
            && !fieldValue.isNull()) {

          collectedIds.add(UUID.fromString(fieldValue.asText()));
        }

        collectRelatedIds(fieldValue, collectedIds);
      }
      return;
    }

    if (currentNode.isArray()) {
      for (JsonNode element : currentNode) {
        collectRelatedIds(element, collectedIds);
      }
    }
  }

  private void addExecutorId(Set<UUID> ids, Map<String, Object> executor) {
    if (executor == null || executor.isEmpty()) return;

    Object id = executor.get("id");
    if (id != null) {
      ids.add(UUID.fromString(id.toString()));
    }
  }
}
