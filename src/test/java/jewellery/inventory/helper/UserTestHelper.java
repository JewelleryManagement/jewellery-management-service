package jewellery.inventory.helper;

import java.util.UUID;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.model.Role;
import jewellery.inventory.model.User;
import org.jetbrains.annotations.NotNull;

public class UserTestHelper {
  public static final String USER_NAME = "john";
  public static final String USER_EMAIL = "john@example.com";
  public static final String USER_PASSWORD = "1P@ssword";

  public static final String FAKE_USER = "fake_user";

  public static User createTestUserWithRandomId() {
    User user = createTestUser();
    user.setId(UUID.randomUUID());
    return user;
  }

  public static User createTestUser() {
    User user = new User();
    user.setName(USER_NAME);
    user.setEmail(USER_EMAIL);
    user.setPassword(USER_PASSWORD);
    user.setRole(Role.ADMIN);
    return user;
  }

  public static User createSecondTestUser() {
    User user = new User();
    user.setName(USER_NAME + 2);
    user.setEmail(USER_EMAIL + 2);
    user.setPassword("pass" + 2);
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
    userRequest.setName(USER_NAME);
    userRequest.setEmail(USER_EMAIL);
    userRequest.setPassword(USER_PASSWORD);
    return userRequest;
  }

  public static UserRequestDto createDifferentUserRequest() {
    UserRequestDto userRequest = new UserRequestDto();
    userRequest.setName("different_user");
    userRequest.setEmail("user@example.com");
    return userRequest;
  }

  public static UserRequestDto createInvalidUserRequest() {
    UserRequestDto invalidUserRequest = new UserRequestDto();
    invalidUserRequest.setName("__");
    invalidUserRequest.setEmail(USER_EMAIL);
    return invalidUserRequest;
  }

  public static @NotNull ResourceInUserRequestDto createResourceInUserRequestDto(
      UUID userId, UUID resourceId, double quantity) {
    ResourceInUserRequestDto requestDto = new ResourceInUserRequestDto();
    requestDto.setUserId(userId);
    requestDto.setResourceId(resourceId);
    requestDto.setQuantity(quantity);
    return requestDto;
  }
}
