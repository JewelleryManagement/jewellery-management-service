package jewellery.inventory.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.UserRequest;
import jewellery.inventory.dto.UserResponse;
import jewellery.inventory.model.User;

public class UserTestHelper {
  public static User createTestUser() {
    User user = new User();
    user.setName("John");
    user.setEmail("john@example.com");
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
    userRequest.setName("John");
    userRequest.setEmail("john@example.com");
    return userRequest;
  }

  public static UserRequest createInvalidUserRequest() {
    UserRequest invalidUserRequest = new UserRequest();
    invalidUserRequest.setName("__");
    invalidUserRequest.setEmail("valid@mail.com");
    return invalidUserRequest;
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
