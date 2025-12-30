package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ResourceTestHelper.getPearl;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.*;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
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
