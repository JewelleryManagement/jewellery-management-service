package jewellery.inventory.controller;

import static jewellery.inventory.helper.UserTestHelper.createInvalidUserRequest;
import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static jewellery.inventory.helper.UserTestHelper.createTestUserWithStaticId;
import static jewellery.inventory.helper.UserTestHelper.jsonToListOfUserResponse;
import static jewellery.inventory.helper.UserTestHelper.jsonToUserResponse;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.UserRequest;
import jewellery.inventory.dto.UserResponse;
import jewellery.inventory.exception.DuplicateEmailException;
import jewellery.inventory.exception.DuplicateNameException;
import jewellery.inventory.exception.UserNotFoundException;
import jewellery.inventory.model.User;
import jewellery.inventory.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(UserController.class)
class UserControllerTest {
  @MockBean private UserService userService;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private User user;
  private UserRequest userRequest;
  private UUID userId;

  private static final String USERS_PATH = "/users";
  private static final String USER_NOT_FOUND_ERROR_MSG = "User not found with id: ";

  @BeforeEach
  public void setup() {
    userId = UUID.randomUUID();
    user = createTestUser();
    userRequest = createTestUserRequest();
  }

  @Test
  void shouldReturnAllUsers() throws Exception {
    when(userService.getAllUsers()).thenReturn(Collections.singletonList(user));

    MvcResult mvcResult = mockMvc.perform(get(USERS_PATH)).andExpect(status().isOk()).andReturn();
    List<UserResponse> userList =
        jsonToListOfUserResponse(mvcResult.getResponse().getContentAsString(), objectMapper);

    assertEquals(1, userList.size());
    assertEquals(user.getId(), userList.get(0).getId());
  }

  @Test
  void shouldReturnUserById() throws Exception {
    when(userService.getUser(userId)).thenReturn(user);

    MvcResult mvcResult =
        mockMvc.perform(get(USERS_PATH + "/{id}", userId)).andExpect(status().isOk()).andReturn();
    String responseBody = mvcResult.getResponse().getContentAsString();
    UserResponse userFromResponse = jsonToUserResponse(responseBody, objectMapper);

    assertEquals(user.getId(), userFromResponse.getId());
  }

  @Test
  void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
    when(userService.getUser(userId))
        .thenThrow(new UserNotFoundException(USER_NOT_FOUND_ERROR_MSG + userId));

    mockMvc.perform(get(USERS_PATH + "/{id}", userId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldCreateNewUser() throws Exception {
    when(userService.createUser(any(User.class))).thenReturn(user);

    MvcResult mvcResult =
        mockMvc
            .perform(
                post(USERS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    UserResponse userFromResponse = jsonToUserResponse(responseBody, objectMapper);

    assertEquals(user.getId(), userFromResponse.getId());
    assertEquals(userRequest.getName(), userFromResponse.getName());
    assertEquals(userRequest.getEmail(), userFromResponse.getEmail());
  }

  @Test
  void shouldReturnBadRequestWhenCreatingUserWithInvalidName() throws Exception {
    userRequest = createInvalidUserRequest();
    userRequest.setName("__"); // Name can't have consecutive underscores
    userRequest.setEmail("valid@mail.com"); // valid email
    mockMvc
        .perform(
            post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestWhenCreatingUserWithInvalidEmail() throws Exception {
    User invalidUser = new User();
    invalidUser.setName("peter"); // valid name
    invalidUser.setEmail("invalid-mail.com"); // invalid email - no @

    mockMvc
        .perform(
            post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestWhenCreatingUserWithExistingName() throws Exception {
    when(userService.createUser(any(User.class)))
        .thenThrow(new DuplicateNameException());

    mockMvc
        .perform(
            post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("User with that name already exists")));
  }

  @Test
  void shouldReturnBadRequestWhenCreatingUserWithExistingEmail() throws Exception {
    when(userService.createUser(any(User.class)))
        .thenThrow(new DuplicateEmailException());

    mockMvc
        .perform(
            post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("User with that email already exists")));
  }

  @Test
  void shouldUpdateUser() throws Exception {
    User updatedUser = createTestUserWithStaticId();
    when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

    MvcResult mvcResult =
        mockMvc
            .perform(
                put(USERS_PATH + "/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    UserResponse userFromResponse = objectMapper.readValue(responseBody, UserResponse.class);

    assertEquals(updatedUser.getId(), userFromResponse.getId());
    assertEquals(updatedUser.getName(), userFromResponse.getName());
    assertEquals(updatedUser.getEmail(), userFromResponse.getEmail());
  }

  @Test
  void shouldReturnNotFoundWhenUpdatingNonExistingUser() throws Exception {
    when(userService.updateUser(any(User.class)))
        .thenThrow(new UserNotFoundException(USER_NOT_FOUND_ERROR_MSG + userId));

    mockMvc
        .perform(
            put(USERS_PATH + "/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnBadRequestWhenUpdatingUserWithExistingName() throws Exception {
    when(userService.updateUser(any(User.class)))
        .thenThrow(new DuplicateNameException());

    mockMvc
        .perform(
            put(USERS_PATH + "/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("User with that name already exists")));
  }

  @Test
  void shouldReturnBadRequestWhenUpdatingUserWithExistingEmail() throws Exception {
    when(userService.updateUser(any(User.class)))
        .thenThrow(new DuplicateEmailException());

    mockMvc
        .perform(
            put(USERS_PATH + "/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("User with that email already exists")));
  }

  @Test
  void shouldDeleteUser() throws Exception {
    doNothing().when(userService).deleteUser(userId);
    mockMvc.perform(delete(USERS_PATH + "/{id}", userId)).andExpect(status().isNoContent());
  }

  @Test
  void shouldReturnNotFoundWhenDeletingNonExistingUser() throws Exception {
    doThrow(new UserNotFoundException(USER_NOT_FOUND_ERROR_MSG + userId))
        .when(userService)
        .deleteUser(userId);

    mockMvc.perform(delete(USERS_PATH + "/{id}", userId)).andExpect(status().isNotFound());
  }
}
