package jewellery.inventory.aspect.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jewellery.inventory.dto.EntityLogDto;
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
  public Map<String, Object> createPayload(Object entity, EventType eventType) {

    String action = determineAction(eventType);
    String entityName = getEntityNameFromClass(entity.getClass());

    EntityLogDto entityLogDto = entityLogMapper.mapEntityToDto(entity);
    entityLogDto.setMessage(entityName + " with ID: " + entityLogDto.getId() + action);
    entityLogDto.setTimestamp(formatCurrentTimestamp());

    return objectMapper.convertValue(entityLogDto, new TypeReference<>() {});
  }

  @Override
  public Map<String, Object> createUpdatePayload(
      Object updatedEntity, Object oldEntity, EventType type) throws Exception {
    String entityName = getEntityNameFromClass(updatedEntity.getClass());

    EntityLogDto entityLogDto = entityLogMapper.mapEntityToDto(updatedEntity);
    entityLogDto.setMessage(entityName + " with ID: " + entityLogDto.getId() + " updated");
    entityLogDto.setTimestamp(formatCurrentTimestamp());

    if (oldEntity != null) {
      logEntityUpdate(entityLogDto, oldEntity);
    }
    return objectMapper.convertValue(entityLogDto, new TypeReference<>() {});
  }

  public void logEntityUpdate(EntityLogDto entityLogDto, Object oldEntity) throws Exception {
    Class<?> entityClass = oldEntity.getClass();
    Class<?> logDtoClass = entityLogDto.getClass();
    List<Field> allFields = getAllFields(entityClass);

    for (Field entityField : allFields) {
      String fieldName = entityField.getName();
      if (fieldName.equalsIgnoreCase("id")) {
        continue;
      }
      String methodNameSuffix = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

      String getterMethodName = "get" + methodNameSuffix;
      String setterMethodName = "set" + methodNameSuffix + "BeforeUpdate";

      try {
        Method getterMethod = entityClass.getMethod(getterMethodName);
        Method setterMethod = logDtoClass.getMethod(setterMethodName, entityField.getType());

        Object oldValue = getterMethod.invoke(oldEntity);
        setterMethod.invoke(entityLogDto, oldValue);
      } catch (NoSuchMethodException e) {
        System.err.println("Method not found: " + e.getMessage());
      }
    }
  }

  public List<Field> getAllFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
    Class<?> parentClass = clazz.getSuperclass();

    if (parentClass != null && !parentClass.equals(Object.class)) {
      fields.addAll(Arrays.asList(parentClass.getDeclaredFields()));
    }

    return fields;
  }

  private String determineAction(EventType type) {
    return switch (type.name()) {
      case "ENTITY_CREATION" -> " created";
      case "ENTITY_DELETION" -> " deleted";
      case "ENTITY_UPDATE" -> " updated";
      default -> throw new IllegalArgumentException("Unknown type: " + type.name());
    };
  }
}
