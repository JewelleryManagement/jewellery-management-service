package jewellery.inventory.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.exceptions.UserNotFoundException;
import jewellery.inventory.model.User;
import jewellery.inventory.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  private User user;
  private UUID userId;
  private ObjectMapper objectMapper;
  private static final String USERS_PATH = "/users";

  @BeforeEach
  public void setup() {
    userId = UUID.randomUUID();
    user = new User();
    user.setId(userId);
    user.setName("John");
    user.setEmail("john@example.com");
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldReturnAllUsers() throws Exception {
    when(userService.getAllUsers()).thenReturn(Collections.singletonList(user));

    MvcResult mvcResult = mockMvc.perform(get(USERS_PATH)).andExpect(status().isOk()).andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    List<User> userList = objectMapper.readValue(responseBody, new TypeReference<>() {});

    assertEquals(1, userList.size());
    assertEquals(user.getId(), userList.get(0).getId());
  }

  @Test
  void shouldReturnUserById() throws Exception {
    when(userService.getUser(userId)).thenReturn(user);

    MvcResult mvcResult =
        mockMvc.perform(get(USERS_PATH + "/{id}", userId)).andExpect(status().isOk()).andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    User userFromResponse = objectMapper.readValue(responseBody, User.class);

    assertEquals(user.getId(), userFromResponse.getId());
  }

  @Test
  void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
    when(userService.getUser(userId))
        .thenThrow(new UserNotFoundException("User not found with id: " + userId));

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
                    .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    User userFromResponse = objectMapper.readValue(responseBody, User.class);

    assertEquals(user.getId(), userFromResponse.getId());
  }

  @Test
  void shouldReturnBadRequestWhenCreatingUserWithInvalidName() throws Exception {
    User invalidUser = new User();
    invalidUser.setName("__"); // Name can't have consecutive underscores
    invalidUser.setEmail("invalid@mail.com"); // valid mail
    mockMvc
        .perform(
            post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
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
  void shouldUpdateUser() throws Exception {
    User updatedUser = new User();
    updatedUser.setId(userId);
    updatedUser.setName("Updated_John");
    updatedUser.setEmail("updatedjohn@example.com");

    when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

    MvcResult mvcResult =
        mockMvc
            .perform(
                put(USERS_PATH + "/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedUser)))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    User userFromResponse = objectMapper.readValue(responseBody, User.class);

    assertEquals(updatedUser.getId(), userFromResponse.getId());
  }

  @Test
  void shouldReturnNotFoundWhenUpdatingNonExistingUser() throws Exception {
    when(userService.updateUser(any(User.class)))
        .thenThrow(new UserNotFoundException("User not found with id: " + userId));

    mockMvc
        .perform(
            put(USERS_PATH + "/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteUser() throws Exception {
    doNothing().when(userService).deleteUser(userId);

    mockMvc.perform(delete(USERS_PATH + "/{id}", userId)).andExpect(status().isNoContent());
  }

  @Test
  void shouldReturnNotFoundWhenDeletingNonExistingUser() throws Exception {
    doThrow(new UserNotFoundException("User not found with id: " + userId))
        .when(userService)
        .deleteUser(userId);

    mockMvc.perform(delete(USERS_PATH + "/{id}", userId)).andExpect(status().isNotFound());
  }
}
