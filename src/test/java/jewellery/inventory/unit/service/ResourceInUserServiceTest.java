package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ResourceTestHelper.getPearl;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import jewellery.inventory.repository.ResourceRepository;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.service.ResourceInUserService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceInUserServiceTest {
  @Mock private UserRepository userRepository;
  @InjectMocks private ResourceInUserService resourceInUserService;
  @Mock private ResourceRepository resourceRepository;
  @Mock private ResourceInUserRepository resourceInUserRepository;
  @Mock private ResourcesInUserMapper resourcesInUserMapper;
  private User user;
  private User secondUser;
  private Resource resource;

  private UUID userId;
  private UUID secondUserId;
  private UUID resourceId;
  private ResourceInUser resourceInUser;
  private static final BigDecimal INITIAL_QUANTITY =
      new BigDecimal("5").setScale(2, RoundingMode.HALF_UP);
  private static final BigDecimal TEST_DEAL_PRICE =
      new BigDecimal("555.55").setScale(2, RoundingMode.HALF_UP);

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
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

    ResourcePurchaseRequestDto purchaseRequestDto =
        createResourcePurchaseRequest(
            userId,
            resourceId,
            new BigDecimal("16").setScale(2, RoundingMode.HALF_UP),
            TEST_DEAL_PRICE);

    resourceInUserService.addResourceToUser(purchaseRequestDto);

    verify(userRepository, times(1)).findById(userId);
    verify(resourceRepository, times(1)).findById(resourceId);
    verify(resourceInUserRepository, times(1)).save(any(ResourceInUser.class));
  }

  @Test
  void willThrowUserNotFoundExceptionWhenAddResourceToUserAndUserNonexistent() {
    ResourcePurchaseRequestDto requestDto =
        createResourcePurchaseRequest(
            userId,
            resourceId,
            new BigDecimal("10").setScale(2, RoundingMode.HALF_UP),
            TEST_DEAL_PRICE);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class, () -> resourceInUserService.addResourceToUser(requestDto));

    verify(userRepository, times(1)).findById(userId);
    verify(resourceRepository, never()).findById(resourceId);
    verify(resourceInUserRepository, never()).save(any(ResourceInUser.class));
  }

  @Test
  void willThrowResourceNotFoundExceptionWhenAddResourceToUserAndResourceNotFound() {
    ResourcePurchaseRequestDto requestDto =
        createResourcePurchaseRequest(
            userId,
            resourceId,
            new BigDecimal("10").setScale(2, RoundingMode.HALF_UP),
            TEST_DEAL_PRICE);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> resourceInUserService.addResourceToUser(requestDto));

    verify(userRepository, times(1)).findById(userId);
    verify(resourceRepository, times(1)).findById(resourceId);
  }

  @Test
  void willGetResourceInUserSuccessfully() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(resourcesInUserMapper.toResourcesInUserResponseDto(user))
        .thenReturn(ResourcesInUserResponseDto.builder().build());

    resourceInUserService.getAllResourcesFromUser(userId);

    verify(userRepository, times(1)).findById(userId);
    verify(resourcesInUserMapper, times(1)).toResourcesInUserResponseDto(user);
  }

  @Test
  void willThrowUserNotFoundExceptionWhenGetResourceInUserForNonexistentUser() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class, () -> resourceInUserService.getAllResourcesFromUser(userId));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void willThrowUserNotFoundExceptionWhenRemovingResourceFromNonexistentUser() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () ->
            resourceInUserService.removeQuantityFromResource(
                userId, resourceId, new BigDecimal("10").setScale(2, RoundingMode.HALF_UP)));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void willThrowResourceInUserNotFoundExceptionWhenRemoveQuantityOfNotOwnedResource() {
    user.setResourcesOwned(List.of());
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        ResourceInUserNotFoundException.class,
        () ->
            resourceInUserService.removeQuantityFromResource(
                userId, resourceId, new BigDecimal("10").setScale(2, RoundingMode.HALF_UP)));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void willThrowInsufficientResourceQuantityExceptionWhenRemoveQuantityMoreThanAvailable() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        InsufficientResourceQuantityException.class,
        () ->
            resourceInUserService.removeQuantityFromResource(
                userId, resourceId, new BigDecimal("10").setScale(2, RoundingMode.HALF_UP)));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void willRemoveQuantityFromResourceInUserSuccessfully() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    final BigDecimal initialQuantity = resourceInUser.getQuantity();
    final BigDecimal quantityToRemove = new BigDecimal("2").setScale(2, RoundingMode.HALF_UP);

    resourceInUserService.removeQuantityFromResource(userId, resourceId, quantityToRemove);

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(any(User.class));

    BigDecimal expectedQuantity = initialQuantity.subtract(quantityToRemove);
    assertEquals(expectedQuantity, resourceInUser.getQuantity());
  }

  @Test
  void willRemoveResourceFromResourceInUserSuccessfullyWhenExactQuantityPassed() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    resourceInUserService.removeQuantityFromResource(userId, resourceId, INITIAL_QUANTITY);

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(any(User.class));
    assertTrue(user.getResourcesOwned().isEmpty());
  }

  @Test
  void willRemoveResourceInUserSuccessfully() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    resourceInUserService.removeResourceFromUser(userId, resourceId);

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void willThrowUserNotFoundExceptionWhenRemoveFromNonexistentUser() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> resourceInUserService.removeResourceFromUser(userId, resourceId));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void willThrowResourceInUserNotFoundExceptionWhenRemoveResourceNotOwnedByUser() {
    UUID nonExistentResourceId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        ResourceInUserNotFoundException.class,
        () -> resourceInUserService.removeResourceFromUser(userId, nonExistentResourceId));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void willThrowUserNotFoundExceptionWhenTransferFromNonexistentUser() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> resourceInUserService.transferResources(getTransferResourceRequestDto()));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void
      willThrowInsufficientResourceQuantityExceptionWhenTransferResourceIsLessThanResourceInUser() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.findById(secondUserId)).thenReturn(Optional.of(secondUser));

    assertThrows(
        InsufficientResourceQuantityException.class,
        () -> resourceInUserService.transferResources(getTransferResourceRequestDto()));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void willThrowResourceInUserNotFoundExceptionWhenTransferResourceNotOwnedByUser() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.findById(secondUserId)).thenReturn(Optional.of(secondUser));

    TransferResourceRequestDto transferResourceRequestDto = getTransferResourceRequestDto();
    transferResourceRequestDto.setTransferredResourceId(UUID.randomUUID());

    assertThrows(
        ResourceInUserNotFoundException.class,
        () -> resourceInUserService.transferResources(transferResourceRequestDto));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }

  @NotNull
  private TransferResourceRequestDto getTransferResourceRequestDto() {
    TransferResourceRequestDto transferResourceRequestDto = new TransferResourceRequestDto();
    transferResourceRequestDto.setPreviousOwnerId(userId);
    transferResourceRequestDto.setNewOwnerId(secondUserId);
    transferResourceRequestDto.setTransferredResourceId(resourceId);
    transferResourceRequestDto.setQuantity(new BigDecimal("6").setScale(2, RoundingMode.HALF_UP));
    return transferResourceRequestDto;
  }
}
