package jewellery.inventory.helper;

import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.dto.request.ResourcePurchaseRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.UserUpdateRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.Role;
import jewellery.inventory.model.User;
import org.jetbrains.annotations.NotNull;

public class UserTestHelper {
  public static final String FIRST_NAME = "john";
  public static final String LAST_NAME = "doe";
  public static final String USER_EMAIL = "john@example.com";
  public static final String USER_PASSWORD = "1P@ssword";

  public static User createTestUserWithRandomId() {
    User user = createTestUser();
    user.setId(UUID.randomUUID());
    return user;
  }

  public static UserRequestDto getTestUserRequest(User user) {
    UserRequestDto userRequestDto = new UserRequestDto();
    userRequestDto.setRole(user.getRole());
    userRequestDto.setPassword(user.getPassword());
    userRequestDto.setEmail(user.getEmail());
    userRequestDto.setFirstName(user.getFirstName());
    userRequestDto.setLastName(user.getLastName());
    return userRequestDto;
  }

  public static User createTestUser() {
    User user = new User();
    user.setFirstName(FIRST_NAME);
    user.setLastName(LAST_NAME);
    user.setEmail(USER_EMAIL);
    user.setPassword(USER_PASSWORD);
    user.setRole(Role.ADMIN);
    return user;
  }

  public static User createUserFromUserUpdateRequestDto(UserUpdateRequestDto userUpdateRequestDto) {
    User user = new User();
    user.setFirstName(userUpdateRequestDto.getFirstName());
    user.setLastName(userUpdateRequestDto.getLastName());
    user.setEmail(userUpdateRequestDto.getEmail());
    user.setNote(userUpdateRequestDto.getNote());
    return user;
  }

  public static UserResponseDto createTestUserResponseDto(User user) {
    UserResponseDto userResponseDto = new UserResponseDto();
    userResponseDto.setId(user.getId());
    userResponseDto.setEmail(user.getEmail());
    userResponseDto.setFirstName(user.getFirstName());
    userResponseDto.setLastName(user.getLastName());
    return userResponseDto;
  }

  public static User createSecondTestUser() {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setFirstName(FIRST_NAME + 2);
    user.setLastName(LAST_NAME + 2);
    user.setEmail(USER_EMAIL + 2);
    user.setPassword(USER_PASSWORD + 2);
    user.setRole(Role.ADMIN);
    return user;
  }

  public static User createTestUserWithId() {
    User user = createTestUser();
    user.setId(UUID.randomUUID());
    return user;
  }

  public static UserRequestDto createTestUserRequest() {
    UserRequestDto userRequest = new UserRequestDto();
    userRequest.setFirstName(FIRST_NAME);
    userRequest.setLastName(LAST_NAME);
    userRequest.setEmail(USER_EMAIL);
    userRequest.setPassword(USER_PASSWORD);
    userRequest.setRole(Role.ADMIN);
    return userRequest;
  }

  public static UserRequestDto createDifferentTestUserRequest() {
    UserRequestDto userRequest = new UserRequestDto();
    userRequest.setFirstName(FIRST_NAME + "different");
    userRequest.setLastName(LAST_NAME + "different");
    userRequest.setEmail("+different" + USER_EMAIL);
    userRequest.setPassword(USER_PASSWORD);
    userRequest.setRole(Role.ADMIN);
    return userRequest;
  }

  public static UserRequestDto createDifferentUserRequest() {
    UserRequestDto userRequest = new UserRequestDto();
    userRequest.setFirstName("different_first_name");
    userRequest.setLastName("different_last_name");
    userRequest.setEmail("user@example.com");
    userRequest.setPassword(USER_PASSWORD);
    return userRequest;
  }

  public static UserUpdateRequestDto createUpdateUserRequest() {
    UserRequestDto userRequest = new UserRequestDto();
    userRequest.setFirstName("changed_firstName");
    userRequest.setLastName("changed_lastName");
    userRequest.setEmail("changed@mail.com");
    userRequest.setNote("Some note for the update");
    return userRequest;
  }

  public static UserRequestDto createInvalidUserRequest() {
    UserRequestDto invalidUserRequest = new UserRequestDto();
    invalidUserRequest.setFirstName("__");
    invalidUserRequest.setEmail(USER_EMAIL);
    return invalidUserRequest;
  }

  public static @NotNull ResourcePurchaseRequestDto createResourcePurchaseRequestDto(
      UUID userId, UUID resourceId, BigDecimal quantity, BigDecimal price) {
    ResourcePurchaseRequestDto requestDto = new ResourcePurchaseRequestDto();
    requestDto.setUserId(userId);
    requestDto.setResourceId(resourceId);
    requestDto.setQuantity(quantity);
    requestDto.setDealPrice(price);
    return requestDto;
  }

  public static ResourcePurchaseRequestDto createResourcePurchaseRequest(
      UUID userId, UUID resourceId, BigDecimal quantity, BigDecimal price) {
    return ResourcePurchaseRequestDto.builder()
        .userId(userId)
        .resourceId(resourceId)
        .quantity(quantity)
        .dealPrice(price)
        .build();
  }
}
