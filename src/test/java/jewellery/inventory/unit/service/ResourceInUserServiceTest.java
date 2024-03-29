package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ResourceTestHelper.getPearl;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.*;
import jewellery.inventory.dto.request.ResourcePurchaseRequestDto;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.not_found.ResourceInUserNotFoundException;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.ResourcesInUserMapper;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceInUserRepository;
import jewellery.inventory.service.ResourceInUserService;
import jewellery.inventory.service.ResourceService;
import jewellery.inventory.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceInUserServiceTest {
  @Mock private UserService userService;
  @Mock private ResourceService resourceService;
  @InjectMocks private ResourceInUserService resourceInUserService;
  @Mock private ResourceInUserRepository resourceInUserRepository;
  @Mock private ResourcesInUserMapper resourcesInUserMapper;
  private User user;
  private User secondUser;
  private Resource resource;

  private UUID userId;
  private UUID secondUserId;
  private UUID resourceId;
  private ResourceInUser resourceInUser;
  private static final BigDecimal INITIAL_QUANTITY = getBigDecimal("5");
  private static final BigDecimal TEST_DEAL_PRICE = getBigDecimal("555.55");

  @BeforeEach
  void setUp() {
    user = createTestUserWithRandomId();
    secondUser = createTestUserWithRandomId();
    resource = getPearl();
    resourceInUser = getResourceInUser();
    user.setResourcesOwned(new ArrayList<>(Collections.singletonList(resourceInUser)));

    resourceId = resource.getId();
    userId = user.getId();
    secondUserId = secondUser.getId();
  }

  private ResourceInUser getResourceInUser() {
    return ResourceInUser.builder()
        .id(UUID.randomUUID())
        .owner(user)
        .resource(resource)
        .quantity(INITIAL_QUANTITY)
        .build();
  }

  @Test
  void willAddResourceToUser() {
    user.setResourcesOwned(new ArrayList<>());
    when(userService.getUser(userId)).thenReturn(user);
    when(resourceService.getResourceById(resourceId)).thenReturn(resource);

    ResourcePurchaseRequestDto purchaseRequestDto =
        createResourcePurchaseRequest(userId, resourceId, getBigDecimal("16"), TEST_DEAL_PRICE);

    resourceInUserService.addResourceToUser(purchaseRequestDto);

    verify(userService, times(1)).getUser(userId);
    verify(resourceService, times(1)).getResourceById(resourceId);
    verify(resourceInUserRepository, times(1)).save(any(ResourceInUser.class));
  }

  @Test
  void willThrowUserNotFoundExceptionWhenAddResourceToUserAndUserNonexistent() {
    ResourcePurchaseRequestDto requestDto =
        createResourcePurchaseRequest(userId, resourceId, getBigDecimal("10"), TEST_DEAL_PRICE);
    when(userService.getUser(userId)).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class, () -> resourceInUserService.addResourceToUser(requestDto));

    verify(userService, times(1)).getUser(userId);
    verify(resourceService, never()).getResourceById(resourceId);
    verify(resourceInUserRepository, never()).save(any(ResourceInUser.class));
  }

  @Test
  void willThrowResourceNotFoundExceptionWhenAddResourceToUserAndResourceNotFound() {
    ResourcePurchaseRequestDto requestDto =
        createResourcePurchaseRequest(userId, resourceId, getBigDecimal("10"), TEST_DEAL_PRICE);
    when(userService.getUser(userId)).thenReturn(user);
    when(resourceService.getResourceById(resourceId)).thenThrow(ResourceNotFoundException.class);

    assertThrows(
        ResourceNotFoundException.class, () -> resourceInUserService.addResourceToUser(requestDto));

    verify(userService, times(1)).getUser(userId);
    verify(resourceService, times(1)).getResourceById(resourceId);
  }

  @Test
  void willGetResourceInUserSuccessfully() {
    when(userService.getUser(userId)).thenReturn(user);
    when(resourcesInUserMapper.toResourcesInUserResponseDto(user))
        .thenReturn(ResourcesInUserResponseDto.builder().build());

    resourceInUserService.getAllResourcesFromUser(userId);

    verify(userService, times(1)).getUser(userId);
    verify(resourcesInUserMapper, times(1)).toResourcesInUserResponseDto(user);
  }

  @Test
  void willThrowUserNotFoundExceptionWhenGetResourceInUserForNonexistentUser() {
    when(userService.getUser(userId)).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class, () -> resourceInUserService.getAllResourcesFromUser(userId));

    verify(userService, times(1)).getUser(userId);
  }

  @Test
  void willThrowUserNotFoundExceptionWhenRemovingResourceFromNonexistentUser() {
    when(userService.getUser(userId)).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () ->
            resourceInUserService.removeQuantityFromResource(
                userId, resourceId, getBigDecimal("10")));

    verify(userService, times(1)).getUser(userId);
  }

  @Test
  void willThrowResourceInUserNotFoundExceptionWhenRemoveQuantityOfNotOwnedResource() {
    user.setResourcesOwned(List.of());
    when(userService.getUser(userId)).thenReturn(user);

    assertThrows(
        ResourceInUserNotFoundException.class,
        () ->
            resourceInUserService.removeQuantityFromResource(
                userId, resourceId, getBigDecimal("10")));

    verify(userService, times(1)).getUser(userId);
  }

  @Test
  void willThrowInsufficientResourceQuantityExceptionWhenRemoveQuantityMoreThanAvailable() {
    when(userService.getUser(userId)).thenReturn(user);

    assertThrows(
        InsufficientResourceQuantityException.class,
        () ->
            resourceInUserService.removeQuantityFromResource(
                userId, resourceId, getBigDecimal("10")));

    verify(userService, times(1)).getUser(userId);
  }

  @Test
  void willRemoveQuantityFromResourceInUserSuccessfully() {
    when(userService.getUser(userId)).thenReturn(user);
    final BigDecimal initialQuantity = resourceInUser.getQuantity();
    final BigDecimal quantityToRemove = getBigDecimal("2");

    resourceInUserService.removeQuantityFromResource(userId, resourceId, quantityToRemove);

    verify(userService, times(1)).getUser(userId);
    verify(userService, times(1)).saveUser(any(User.class));

    BigDecimal expectedQuantity = initialQuantity.subtract(quantityToRemove);
    assertEquals(expectedQuantity, resourceInUser.getQuantity());
  }

  @Test
  void willRemoveResourceFromResourceInUserSuccessfullyWhenExactQuantityPassed() {
    when(userService.getUser(userId)).thenReturn(user);

    resourceInUserService.removeQuantityFromResource(userId, resourceId, INITIAL_QUANTITY);

    verify(userService, times(1)).getUser(userId);
    verify(userService, times(1)).saveUser(any(User.class));
    assertTrue(user.getResourcesOwned().isEmpty());
  }

  @Test
  void willRemoveResourceInUserSuccessfully() {
    when(userService.getUser(userId)).thenReturn(user);

    resourceInUserService.removeResourceFromUser(userId, resourceId);

    verify(userService, times(1)).getUser(userId);
    verify(userService, times(1)).saveUser(any(User.class));
  }

  @Test
  void willThrowUserNotFoundExceptionWhenRemoveFromNonexistentUser() {
    when(userService.getUser(userId)).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () -> resourceInUserService.removeResourceFromUser(userId, resourceId));

    verify(userService, times(1)).getUser(userId);
    verify(userService, never()).saveUser(any(User.class));
  }

  @Test
  void willThrowResourceInUserNotFoundExceptionWhenRemoveResourceNotOwnedByUser() {
    UUID nonExistentResourceId = UUID.randomUUID();

    when(userService.getUser(userId)).thenReturn(user);

    assertThrows(
        ResourceInUserNotFoundException.class,
        () -> resourceInUserService.removeResourceFromUser(userId, nonExistentResourceId));

    verify(userService, times(1)).getUser(userId);
    verify(userService, never()).saveUser(any(User.class));
  }

  @Test
  void willThrowUserNotFoundExceptionWhenTransferFromNonexistentUser() {
    when(userService.getUser(userId)).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () -> resourceInUserService.transferResources(getTransferResourceRequestDto()));

    verify(userService, times(1)).getUser(userId);
    verify(userService, never()).saveUser(any(User.class));
  }

  @Test
  void
      willThrowInsufficientResourceQuantityExceptionWhenTransferResourceIsLessThanResourceInUser() {
    when(userService.getUser(userId)).thenReturn(user);
    when(userService.getUser(secondUserId)).thenReturn(secondUser);

    assertThrows(
        InsufficientResourceQuantityException.class,
        () -> resourceInUserService.transferResources(getTransferResourceRequestDto()));

    verify(userService, times(1)).getUser(userId);
    verify(userService, never()).saveUser(any(User.class));
  }

  @Test
  void willThrowResourceInUserNotFoundExceptionWhenTransferResourceNotOwnedByUser() {
    when(userService.getUser(userId)).thenReturn(user);
    when(userService.getUser(secondUserId)).thenReturn(secondUser);

    TransferResourceRequestDto transferResourceRequestDto = getTransferResourceRequestDto();
    transferResourceRequestDto.setTransferredResourceId(UUID.randomUUID());

    assertThrows(
        ResourceInUserNotFoundException.class,
        () -> resourceInUserService.transferResources(transferResourceRequestDto));

    verify(userService, times(1)).getUser(userId);
    verify(userService, never()).saveUser(any(User.class));
  }

  @NotNull
  private TransferResourceRequestDto getTransferResourceRequestDto() {
    TransferResourceRequestDto transferResourceRequestDto = new TransferResourceRequestDto();
    transferResourceRequestDto.setPreviousOwnerId(userId);
    transferResourceRequestDto.setNewOwnerId(secondUserId);
    transferResourceRequestDto.setTransferredResourceId(resourceId);
    transferResourceRequestDto.setQuantity(getBigDecimal("6"));
    return transferResourceRequestDto;
  }
}
