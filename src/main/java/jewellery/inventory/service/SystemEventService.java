package jewellery.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.TransferResourceResponseDto;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemEventService {

  private final SystemEventRepository systemEventRepository;
  private final JwtUtils jwtUtils;
  private final ObjectMapper objectMapper;

  public List<SystemEvent> getAllEvents() {
    return systemEventRepository.findAll();
  }

  @Transactional
  public <T, U> void logEvent(EventType type, T entity, @Nullable U oldEntity) {
    Map<String, Object> payload = new HashMap<>();

    if ((type.name().contains("_UPDATE") || type.name().contains("_QUANTITY"))
        && oldEntity != null) {
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

  @Transactional
  public void logResourceTransfer(TransferResourceResponseDto transferResult, EventType eventType) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("previousOwner", transferResult.getPreviousOwner());
    payload.put("newOwner", transferResult.getNewOwner());
    payload.put("transferredResource", transferResult.getTransferredResource());

    logEvent(eventType, payload, null);
  }

  @Transactional
  public void logProductTransfer(ProductResponseDto productResult, EventType eventType) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("newOwner", productResult.getOwner());
    payload.put("transferredProduct", productResult);

    logEvent(eventType, payload, null);
  }
}
