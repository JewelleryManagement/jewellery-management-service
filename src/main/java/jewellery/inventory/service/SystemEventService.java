package jewellery.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemEventService {

  private final SystemEventRepository systemEventRepository;
  private final JwtUtils jwtUtils;
  private final ObjectMapper objectMapper;

  public List<SystemEvent> getAllEvents() {
    return systemEventRepository.findAll();
  }

  public <T, U> void logEvent(EventType type, T entity, @Nullable U oldEntity) {
    Map<String, Object> payload = new HashMap<>();

    payload.put("entityBefore", createMap(oldEntity));
    payload.put("entityAfter", createMap(entity));

    logEvent(type, payload);
  }

  public <T, U> void logEvent(EventType type, T entity) {
    Map<String, Object> payload = new HashMap<>();

    payload.put("entity", createMap(entity));

    logEvent(type, payload);
  }

  private <U> Object createMap(U entity) {
    return objectMapper.convertValue(entity, new TypeReference<>() {});
  }

  private void logEvent(EventType type, Map<String, Object> payload) {
    SystemEvent event = new SystemEvent();
    Object executor = jwtUtils.getCurrentUser();
    if (executor != null) {
      Map<String, Object> executorMap =
          objectMapper.convertValue(executor, new TypeReference<>() {});
      event.setExecutor(executorMap);
    }

    event.setType(type);
    event.setTimestamp(Instant.now());
    event.setPayload(payload);

    systemEventRepository.save(event);
  }
}
