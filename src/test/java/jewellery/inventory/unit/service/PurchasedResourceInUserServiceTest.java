package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ResourceTestHelper.getPearl;
import static jewellery.inventory.helper.ResourceTestHelper.getPearlResponseDto;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.*;
import jewellery.inventory.dto.response.PurchasedResourceQuantityResponseDto;
import jewellery.inventory.dto.response.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.PurchasedResourceInUserMapper;
import jewellery.inventory.model.PurchasedResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.PurchasedResourceInUserRepository;
import jewellery.inventory.service.PurchasedResourceInUserService;
import jewellery.inventory.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurchasedResourceInUserServiceTest {
  @Mock private UserService userService;
  @InjectMocks private PurchasedResourceInUserService purchasedResourceInUserService;
  @Mock private PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  @Mock private PurchasedResourceInUserMapper purchasedResourceInUserMapper;
  private User user;
  private Resource resource;
  private ResourceResponseDto resourceResponseDto;
  private PurchasedResourceQuantityResponseDto purchasedResourceQuantityResponseDto1;
  private PurchasedResourceQuantityResponseDto purchasedResourceQuantityResponseDto2;

  private UUID userId;
  private PurchasedResourceInUser purchasedResourceInUser1;
  private PurchasedResourceInUser purchasedResourceInUser2;
  private static final BigDecimal INITIAL_QUANTITY = getBigDecimal("5");
  private static final BigDecimal SALE_PRICE = getBigDecimal("20");
  private static final BigDecimal DISCOUNT = getBigDecimal("2");

  @BeforeEach
  void setUp() {
    user = createTestUserWithRandomId();
    resource = getPearl();
    resourceResponseDto = getPearlResponseDto();
    purchasedResourceInUser1 = getPurchasedResourceInUser();
    purchasedResourceInUser2 = getPurchasedResourceInUser();
    purchasedResourceQuantityResponseDto1 = getPurchasedResourceQuantityResponseDto();
    purchasedResourceQuantityResponseDto2 = getPurchasedResourceQuantityResponseDto();

    userId = user.getId();
  }

  @Test
  void getAllPurchasedResourcesSuccessfully() {
    when(userService.getUser(userId)).thenReturn(user);
    List<PurchasedResourceInUser> purchasedResourcesInUsers =
        List.of(purchasedResourceInUser1, purchasedResourceInUser2);
    when(purchasedResourceInUserRepository.findAllByOwnerId(userId))
        .thenReturn(purchasedResourcesInUsers);
    when(purchasedResourceInUserMapper.toPurchasedResourceQuantityResponseDto(
            purchasedResourceInUser1))
        .thenReturn(purchasedResourceQuantityResponseDto1);
    when(purchasedResourceInUserMapper.toPurchasedResourceQuantityResponseDto(
            purchasedResourceInUser2))
        .thenReturn(purchasedResourceQuantityResponseDto2);

    List<PurchasedResourceQuantityResponseDto> allPurchasedResources =
        purchasedResourceInUserService.getAllPurchasedResources(userId);

    assertNotNull(allPurchasedResources);
    assertEquals(2, allPurchasedResources.size());
    assertEquals(allPurchasedResources.get(0), purchasedResourceQuantityResponseDto1);
    assertEquals(allPurchasedResources.get(1), purchasedResourceQuantityResponseDto2);

    verify(userService).getUser(userId);
    verify(purchasedResourceInUserRepository).findAllByOwnerId(userId);
    verify(purchasedResourceInUserMapper)
        .toPurchasedResourceQuantityResponseDto(purchasedResourceInUser1);
    verify(purchasedResourceInUserMapper)
        .toPurchasedResourceQuantityResponseDto(purchasedResourceInUser2);
  }

  @Test
  void getAllPurchasedResourcesThrowsExceptionWhenUserNotFound() {
    when(userService.getUser(userId)).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () -> purchasedResourceInUserService.getAllPurchasedResources(userId));
  }

  @Test
  void getAllPurchasedResourcesReturnsEmptyListWhenThereAreNoPurchasedResources() {
    when(userService.getUser(userId)).thenReturn(user);
    when(purchasedResourceInUserRepository.findAllByOwnerId(user.getId())).thenReturn(List.of());

    List<PurchasedResourceQuantityResponseDto> allPurchasedResources =
        purchasedResourceInUserService.getAllPurchasedResources(userId);

    assertNotNull(allPurchasedResources);
    assertTrue(allPurchasedResources.isEmpty());

    verify(userService).getUser(userId);
    verify(purchasedResourceInUserRepository).findAllByOwnerId(userId);
  }

  private PurchasedResourceInUser getPurchasedResourceInUser() {
    return PurchasedResourceInUser.builder()
        .id(UUID.randomUUID())
        .resource(resource)
        .owner(user)
        .quantity(INITIAL_QUANTITY)
        .salePrice(SALE_PRICE)
        .discount(DISCOUNT)
        .build();
  }

  private PurchasedResourceQuantityResponseDto getPurchasedResourceQuantityResponseDto() {
    return PurchasedResourceQuantityResponseDto.builder()
        .resourceAndQuantity(new ResourceQuantityResponseDto(resourceResponseDto, INITIAL_QUANTITY))
        .salePrice(SALE_PRICE)
        .discount(DISCOUNT)
        .build();
  }
}
