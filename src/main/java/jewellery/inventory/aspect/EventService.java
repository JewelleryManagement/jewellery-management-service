package jewellery.inventory.aspect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
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
public class EventService {

  private final SystemEventRepository systemEventRepository;
  private final JwtUtils jwtUtils;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public <T, U> void logEvent(EventType type, T entity, @Nullable U oldEntity) {
    Map<String, Object> payload = new HashMap<>();

    if (type.name().contains("_UPDATE") && oldEntity != null) {
      payload.put("entityBefore", oldEntity);
      payload.put("entityAfter", entity);
    } else {
      payload.put("entity", entity);
    }

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
