package jewellery.inventory.aspect.strategy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.GemstoneResponseDto;
import jewellery.inventory.dto.response.resource.LinkingPartResponseDto;
import jewellery.inventory.dto.response.resource.PearlResponseDto;
import jewellery.inventory.dto.response.resource.PreciousMetalResponseDto;
import jewellery.inventory.model.EventType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class PayloadStrategy<T, U> {

  public abstract Map<String, Object> createPayload(Object entity, EventType type) throws Exception;

  public abstract Map<String, Object> createUpdatePayload(
      Object entity, Object updatedEntity, EventType type) throws Exception;

  protected String formatCurrentTimestamp() {
    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'at' HH:mm:ss 'on' dd.MM.yyyy");
    return localDateTime.format(formatter);
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

  public Class<?> getDtoClass(Class<?> entityClass) {
    return switch (entityClass.getSimpleName()) {
      case "UserResponseDto" -> UserResponseDto.class;
      case "GemstoneResponseDto" -> GemstoneResponseDto.class;
      case "PearlResponseDto" -> PearlResponseDto.class;
      case "LinkingPartResponseDto" -> LinkingPartResponseDto.class;
      case "PreciousMetalResponseDto" -> PreciousMetalResponseDto.class;
      default -> throw new IllegalArgumentException("Unknown entity class: " + entityClass);
    };
  }
}
