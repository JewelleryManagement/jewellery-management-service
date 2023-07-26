package jewellery.inventory.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.UserRequest;
import jewellery.inventory.dto.response.UserResponse;
import jewellery.inventory.model.User;

public class UserTestHelper {
  public static final String USER_NAME = "john";
  public static final String USER_EMAIL = "john@example.com";

  public static User createTestUser() {
    User user = new User();
    user.setName(USER_NAME);
    user.setEmail(USER_EMAIL);
    return user;
  }

  public static User createSecondTestUser() {
    User user = new User();
    user.setName(USER_NAME+2);
    user.setEmail(USER_EMAIL+2);
    return user;
  }

  public static User createTestUserWithId() {
    User user = createTestUser();
    user.setId(UUID.randomUUID());
    return user;
  }

  public static User createTestUserWithStaticId() {
    User user = createTestUser();
    user.setId(UUID.fromString("413c1afa-0f19-4649-bdf7-2f63cbc73998"));
    return user;
  }

  public static UserRequest createTestUserRequest() {
    UserRequest userRequest = new UserRequest();
    userRequest.setName(USER_NAME);
    userRequest.setEmail(USER_EMAIL);
    return userRequest;
  }

  public static UserRequest createDifferentUserRequest() {
    UserRequest userRequest = new UserRequest();
    userRequest.setName("different_user");
    userRequest.setEmail("user@example.com");
    return userRequest;
  }

  public static UserRequest createInvalidUserRequest() {
    UserRequest invalidUserRequest = new UserRequest();
    invalidUserRequest.setName("__");
    invalidUserRequest.setEmail("valid@mail.com");
    return invalidUserRequest;
  }

  public static UserResponse createUserResponse() {
    UserResponse userResponse = new UserResponse();
    userResponse.setName(USER_NAME);
    userResponse.setEmail(USER_EMAIL);
    return userResponse;
  }

  public static UserResponse jsonToUserResponse(String json, ObjectMapper objectMapper) {
    try {
      return objectMapper.readValue(json, UserResponse.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to convert json to UserResponse object", e);
    }
  }

  public static List<UserResponse> jsonToListOfUserResponse(
      String json, ObjectMapper objectMapper) {
    try {
      return objectMapper.readValue(json, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to convert JSON to List<UserResponse>", e);
    }
  }
}
