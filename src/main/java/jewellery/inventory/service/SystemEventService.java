package jewellery.inventory.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jewellery.inventory.dto.response.SystemEventLiteResponseDto;
import jewellery.inventory.dto.response.SystemEventResponseDto;
import jewellery.inventory.exception.forbidden.ForbiddenException;
import jewellery.inventory.exception.not_found.NotFoundException;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.RoleType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.RoleMembershipRepository;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.service.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemEventService {
  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b");

  private final SystemEventRepository systemEventRepository;
  private final AuthService authService;
  private final ObjectMapper objectMapper;
  private final RoleMembershipRepository roleMembershipRepository;
  private final AuthorizationService authorizationService;

  public List<SystemEventLiteResponseDto> getAllEvents() {
    UUID currentUserId = authService.getCurrentUser().getId();

    if (authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name())) {
      return systemEventRepository.findAllWithoutRelatedIds();
    }

    List<UUID> organizationIds =
        roleMembershipRepository.findOrganizationIdsByUserIdAndPermission(
            currentUserId, RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ);

    if (organizationIds.isEmpty()) {
      return List.of();
    }

    return systemEventRepository.findByRelatedOrganizationIds(organizationIds);
  }

  @Transactional(readOnly = true)
  public SystemEventResponseDto getSystemEvent(UUID id) {
    if (!systemEventRepository.existsById(id)) {
      throw new NotFoundException("Event with id: " + id + " is not found!");
    }

    if (authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name())) {
      return systemEventRepository
          .findByIdWithoutRelatedIds(id)
          .orElseThrow(() -> new NotFoundException("Event with id: " + id + " is not found!"));
    }

    UUID currentUserId = authService.getCurrentUser().getId();

    List<UUID> organizationIds =
        roleMembershipRepository.findOrganizationIdsByUserIdAndPermission(
            currentUserId, RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ);

    if (organizationIds.isEmpty()) {
      throw new ForbiddenException("You do not have permission to perform this action");
    }

    return systemEventRepository
        .findByIdAndRelatedOrganizationIds(id, organizationIds)
        .orElseThrow(
            () -> new ForbiddenException("You do not have permission to perform this action"));
  }

  @Transactional(readOnly = true)
  public List<SystemEventLiteResponseDto> getEventsRelatedTo(UUID id) {
    UUID currentUserId = authService.getCurrentUser().getId();

    if (authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name())) {
      return systemEventRepository.findByRelatedId(id);
    }

    List<UUID> organizationIds =
        roleMembershipRepository.findOrganizationIdsByUserIdAndPermission(
            currentUserId, RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ);

    if (organizationIds.isEmpty()) {
      return List.of();
    }

    return systemEventRepository.findByRelatedIdAndRelatedOrganizationIds(id, organizationIds);
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

  private static void collectRelatedIds(JsonNode node, Set<UUID> relatedIds) {
    if (node == null || node.isNull()) return;

    if (node.isObject()) {
      node.fields().forEachRemaining(e -> collectRelatedIds(e.getValue(), relatedIds));
      return;
    }

    if (node.isArray()) {
      node.forEach(child -> collectRelatedIds(child, relatedIds));
      return;
    }

    if (node.isTextual()) {
      Matcher m = UUID_PATTERN.matcher(node.asText());
      while (m.find()) {
        relatedIds.add(UUID.fromString(m.group()));
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
