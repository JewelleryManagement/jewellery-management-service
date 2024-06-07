package jewellery.inventory.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.service.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemEventService {

  private final SystemEventRepository systemEventRepository;
  private final AuthService authService;
  private final ObjectMapper objectMapper;

  public Page<SystemEvent> getAllEventsWithPagination(int page, int pageSize) {
    Page<SystemEvent> systemEventPage =
        systemEventRepository.findAll(PageRequest.of(page, pageSize));
    return new PageImpl<>(
        systemEventPage.getContent(),
        systemEventPage.getPageable(),
        systemEventPage.getTotalElements());
  }

  public List<SystemEvent> getAllEvents() {
    return systemEventRepository.findAll();
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
    event.setExecutor(getCurrentUser());
    event.setType(type);
    event.setTimestamp(Instant.now());
    event.setPayload(payload);

    systemEventRepository.save(event);
  }

  private Map<String, Object> getCurrentUser() {
    Object executor = authService.getCurrentUser();
    return executor != null
        ? objectMapper.convertValue(executor, new TypeReference<>() {})
        : Map.of();
  }
}
