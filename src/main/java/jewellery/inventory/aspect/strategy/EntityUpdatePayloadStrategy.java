package jewellery.inventory.aspect.strategy;

// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import java.lang.reflect.Field;
// import java.lang.reflect.Method;
// import java.util.Map;
// import jewellery.inventory.aspect.EventService;
// import jewellery.inventory.dto.EntityLogDto;
// import jewellery.inventory.mapper.EntityLogMapper;
// import jewellery.inventory.model.EventType;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Component;

// @Component
// @RequiredArgsConstructor
// public class EntityUpdatePayloadStrategy extends PayloadStrategy<Object, Object> {
//  private final ObjectMapper objectMapper = new ObjectMapper();
//  private final EntityLogMapper entityLogMapper;
//
//  @Override
//  public Map<String, Object> createPayload(Object entity, Object updatedEntity, EventType type)
//      throws Exception {
//    String entityName = getEntityNameFromClass(entity.getClass());
//
//    EntityLogDto entityLogDto = entityLogMapper.mapEntityToDto(entity);
//    entityLogDto.setMessage(entityName + " with ID: " + entityLogDto.getId() + " updated");
//    entityLogDto.setTimestamp(formatCurrentTimestamp());
//
//    if (updatedEntity != null) {
//      logEntityUpdate(entityLogDto, entity);
//    }
//    return objectMapper.convertValue(entityLogDto, new TypeReference<>() {});
//  }
//
//  public void logEntityUpdate(EntityLogDto entityLogDto, Object oldEntity) throws Exception {
//    Class<?> entityClass = oldEntity.getClass();
//    Class<?> logDtoClass = entityLogDto.getClass();
//
//    for (Field entityField : entityClass.getDeclaredFields()) {
//      String fieldName = entityField.getName();
//      String methodNameSuffix = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
//
//      String getterMethodName = "get" + methodNameSuffix;
//      String setterMethodName = "set" + methodNameSuffix + "BeforeUpdate";
//
//      try {
//        Method getterMethod = entityClass.getMethod(getterMethodName);
//        Method setterMethod = logDtoClass.getMethod(setterMethodName, entityField.getType());
//
//        Object oldValue = getterMethod.invoke(oldEntity);
//        setterMethod.invoke(entityLogDto, oldValue);
//      } catch (NoSuchMethodException e) {
//        System.err.println("Method not found: " + e.getMessage());
//      }
//    }
//  }
// }
