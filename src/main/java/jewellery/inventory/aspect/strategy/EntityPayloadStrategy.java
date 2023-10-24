package jewellery.inventory.aspect.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import jewellery.inventory.dto.EntityLogDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.GemstoneResponseDto;
import jewellery.inventory.dto.response.resource.LinkingPartResponseDto;
import jewellery.inventory.dto.response.resource.PearlResponseDto;
import jewellery.inventory.dto.response.resource.PreciousMetalResponseDto;
import jewellery.inventory.mapper.EntityLogMapper;
import jewellery.inventory.model.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityPayloadStrategy extends PayloadStrategy<Object, Object> {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final EntityLogMapper entityLogMapper;

  @Override
  public Map<String, Object> createPayload(
      Object entity, Object updatedEntity, EventType eventType) {

    String action = (eventType == EventType.ENTITY_CREATION) ? " created" : " deleted";
    String entityName = getEntityNameFromClass(entity.getClass());

    EntityLogDto entityLogDto = mapEntityToDto(entity);
    entityLogDto.setMessage(entityName + " with ID: " + entityLogDto.getId() + action);
    entityLogDto.setTimestamp(formatCurrentTimestamp());

    return objectMapper.convertValue(entityLogDto, Map.class);
  }

  private EntityLogDto mapEntityToDto(Object entity) {
    return switch (entity.getClass().getSimpleName()) {
      case "UserResponseDto" -> entityLogMapper.toEntityLogDto((UserResponseDto) entity);
      case "ResourcesInUserResponseDto" -> entityLogMapper.toEntityLogDto(
          (ResourcesInUserResponseDto) entity);
      case "PearlResponseDto" -> entityLogMapper.toEntityLogDto((PearlResponseDto) entity);
      case "GemstoneResponseDto" -> entityLogMapper.toEntityLogDto((GemstoneResponseDto) entity);
      case "LinkingPartResponseDto" -> entityLogMapper.toEntityLogDto(
          (LinkingPartResponseDto) entity);
      case "PreciousMetalResponseDto" -> entityLogMapper.toEntityLogDto(
          (PreciousMetalResponseDto) entity);
      default -> throw new IllegalArgumentException(
          "Unsupported entity type: " + entity.getClass());
    };
  }

  public String getEntityNameFromClass(Class<?> clazz) {
    String className = clazz.getSimpleName();
    return switch (className) {
      case "UserResponseDto" -> "User";
      case "ResourcesInUserResponseDto" -> "ResourcesInUser";
      case "ProductResponseDto" -> "Product";
      case "GemstoneResponseDto" -> "Gemstone";
      case "LinkingPartResponseDto" -> "LinkingPart";
      case "PearlResponseDto" -> "Pearl";
      case "PreciousMetalResponseDto" -> "PreciousMetal";
      default -> throw new IllegalArgumentException("Unknown class: " + className);
    };
  }
}
