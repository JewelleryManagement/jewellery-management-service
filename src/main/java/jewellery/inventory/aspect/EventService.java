package jewellery.inventory.aspect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.security.JwtUtils;
import jewellery.inventory.service.ResourceService;
import jewellery.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

  private final SystemEventRepository systemEventRepository;
  private final JwtUtils jwtUtils;
  private final UserService userService;
  private final ResourceService resourceService;
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
    event.setTimestamp(formatCurrentTimestamp());
    event.setPayload(payload);

    systemEventRepository.save(event);
  }

  public Object fetchEntityByIdAsDto(UUID id, Class<?> entityType) {
    if (entityType.equals(UserRequestDto.class)) {
      return userService.fetchByIdAsDto(id);
    } else if (ResourceRequestDto.class.isAssignableFrom(entityType)) {
      return resourceService.fetchByIdAsDto(id);
    }
    throw new IllegalArgumentException("Unsupported entity type: " + entityType);
  }

  protected String formatCurrentTimestamp() {
    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'at' HH:mm:ss 'on' dd.MM.yyyy");
    return localDateTime.format(formatter);
  }
}
