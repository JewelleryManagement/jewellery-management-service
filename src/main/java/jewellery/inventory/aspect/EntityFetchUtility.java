package jewellery.inventory.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ResourceOwnedByUsersResponseDto;
import jewellery.inventory.service.ProductService;
import jewellery.inventory.service.ResourceInUserService;
import jewellery.inventory.service.ResourceService;
import jewellery.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityFetchUtility {
  private static final Logger logger = LoggerFactory.getLogger(EntityFetchUtility.class);
  private static final Map<Class<?>, String> ENTITY_TYPE_TO_METHOD =
      Map.of(
          UserService.class, "getUser",
          ResourceService.class, "getResource",
          ProductService.class, "getProduct",
          ResourceInUserService.class, "getAllResourcesFromUser");

  private static final Map<Class<?>, Class<?>> SERVICE_TO_ENTITY_TYPE =
      Map.of(
          UserService.class, UserRequestDto.class,
          ResourceService.class, ResourceRequestDto.class,
          ProductService.class, ProductRequestDto.class,
          ResourceInUserService.class, ResourceInUserRequestDto.class);

  private final ResourceInUserService resourceInUserService;

  protected Object fetchEntity(Object service, UUID entityId, Class<?> entityType) {
    String methodName = determineFetchMethod(service, entityType);

    try {
      Method method = service.getClass().getMethod(methodName, UUID.class);
      return method.invoke(service, entityId);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      logger.error(
          "Failed to fetch entity for logging: method={}, entityId={}", methodName, entityId, e);
      return null;
    }
  }

  protected Object fetchEntity(Object service, UUID userId) {
    try {
      Method method = service.getClass().getMethod("getAllResourcesFromUser", UUID.class);
      return method.invoke(service, userId);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      logger.error("Unable to fetch entity for logging", e);
      return null;
    }
  }

  protected ResourceOwnedByUsersResponseDto fetchResourceInUser(UUID resourceId) {
    return resourceInUserService.getUsersAndQuantities(resourceId);
  }

  protected Class<?> determineEntityType(Object service) {
    return SERVICE_TO_ENTITY_TYPE.getOrDefault(service.getClass(), null);
  }

  private String determineFetchMethod(Object service, Class<?> entityType) {
    return ENTITY_TYPE_TO_METHOD.getOrDefault(service.getClass(), null);
  }
}
