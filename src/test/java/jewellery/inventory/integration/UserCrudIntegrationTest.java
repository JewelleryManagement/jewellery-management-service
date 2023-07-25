package jewellery.inventory.integration;

import static jewellery.inventory.helper.UserTestHelper.createDifferentUserRequest;
import static jewellery.inventory.helper.UserTestHelper.createInvalidUserRequest;
import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.UserRequest;
import jewellery.inventory.dto.UserResponse;
import jewellery.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class UserCrudIntegrationTest {
  @Value(value = "${local.server.port}")
  private int port;

  private String getBaseUserUrl() {
    return "http://localhost:" + port + "/users";
  }

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired UserRepository userRepository;

  @BeforeEach
  void cleanUp() {
    userRepository.deleteAll();
  }

  @Test
  void willCreateNewUser() {
    UserRequest userRequest = createTestUserRequest();

    ResponseEntity<UserResponse> response =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponse.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    UserResponse userResponse = response.getBody();

    assertNotNull(userResponse);
    assertNotNull(userResponse.getId());
    assertEquals(userRequest.getName(), userResponse.getName());
    assertEquals(userRequest.getEmail(), userResponse.getEmail());
  }

  @Test
  void willNotCreateNewUserWithInvalidName() {
    assertTrue(
        this.testRestTemplate
            .postForEntity(getBaseUserUrl(), createInvalidUserRequest(), UserResponse.class)
            .getStatusCode()
            .is4xxClientError());
  }

  @Test
  void willNotCreateUserWithDuplicateEmail() {
    UserRequest userRequest = createTestUserRequest();

    this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponse.class);

    ResponseEntity<UserResponse> response =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponse.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void willNotCreateUserWithDuplicateName() {
    UserRequest userRequest = createTestUserRequest();

    this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponse.class);

    UserRequest duplicateNameUserRequest = new UserRequest();
    duplicateNameUserRequest.setName(userRequest.getName());
    duplicateNameUserRequest.setEmail("differentEmail@example.com");

    ResponseEntity<UserResponse> response =
        this.testRestTemplate.postForEntity(
            getBaseUserUrl(), duplicateNameUserRequest, UserResponse.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void willGetUsersFromDatabase() throws JsonProcessingException {
    UserRequest userRequest = createTestUserRequest();

    ResponseEntity<UserResponse> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponse.class);
    assertNotNull(userResponseEntity.getBody());

    ResponseEntity<List<UserResponse>> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<UserResponse>>() {});

    assertEquals(HttpStatus.FOUND, response.getStatusCode());

    List<UserResponse> users = response.getBody();
    assertNotNull(users);
    assertFalse(users.isEmpty());
    assertEquals(userRequest.getName(), users.get(0).getName());
    assertEquals(userRequest.getEmail(), users.get(0).getEmail());
  }

  @Test
  void willGetSpecificUser() {
    UserRequest userRequest = createTestUserRequest();
    ResponseEntity<UserResponse> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponse.class);
    UserResponse createdUser = userResponseEntity.getBody();

    ResponseEntity<UserResponse> response =
        this.testRestTemplate.getForEntity(
            getBaseUserUrl() + "/" + createdUser.getId(), UserResponse.class);

    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    UserResponse fetchedUser = response.getBody();

    assertNotNull(fetchedUser);
    assertEquals(createdUser.getId(), fetchedUser.getId());
    assertEquals(userRequest.getName(), fetchedUser.getName());
    assertEquals(userRequest.getEmail(), fetchedUser.getEmail());
  }

  @Test
  void willGetNonexistentUser() {
    UUID randomId = UUID.randomUUID();
    ResponseEntity<String> response =
        this.testRestTemplate.getForEntity(getBaseUserUrl() + "/" + randomId, String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void willUpdateUser() {
    UserRequest userRequest = createTestUserRequest();
    ResponseEntity<UserResponse> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponse.class);
    UserResponse createdUser = userResponseEntity.getBody();

    UserRequest updatedUserRequest = createTestUserRequest();
    HttpEntity<UserRequest> requestUpdate = new HttpEntity<>(updatedUserRequest);
    ResponseEntity<UserResponse> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + createdUser.getId(),
            HttpMethod.PUT,
            requestUpdate,
            UserResponse.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    UserResponse updatedUser = response.getBody();

    assertNotNull(updatedUser);
    assertEquals(createdUser.getId(), updatedUser.getId());
    assertEquals(updatedUserRequest.getName(), updatedUser.getName());
    assertEquals(updatedUserRequest.getEmail(), updatedUser.getEmail());
  }

  @Test
  void willNotUpdateUserWithDuplicateEmail() {

    UserResponse firstUser = sendUserCreateRequest(createTestUserRequest());

    UserResponse secondUser = sendUserCreateRequest(createDifferentUserRequest());

    UserRequest secondUserRequest = new UserRequest();
    secondUserRequest.setName(secondUser.getName());
    secondUserRequest.setEmail(firstUser.getEmail());

    HttpEntity<UserRequest> requestUpdate = new HttpEntity<>(secondUserRequest);
    ResponseEntity<UserResponse> responseEntity =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + secondUser.getId(),
            HttpMethod.PUT,
            requestUpdate,
            UserResponse.class);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
  }

  @Test
  void willNotUpdateUserWithDuplicateName() {
    UserResponse firstUser = sendUserCreateRequest(createTestUserRequest());
    UserResponse secondUser = sendUserCreateRequest(createDifferentUserRequest());

    UserRequest firstUserRequest = createTestUserRequest();
    UserRequest secondUserRequest = createDifferentUserRequest();

    secondUserRequest.setName(firstUserRequest.getName());
    HttpEntity<UserRequest> requestUpdate = new HttpEntity<>(secondUserRequest);
    ResponseEntity<UserResponse> responseEntity =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + secondUser.getId(),
            HttpMethod.PUT,
            requestUpdate,
            UserResponse.class);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
  }

  @Test
  void willNotUpdateNonexistentUser() {
    UserRequest userRequest = createTestUserRequest();
    UUID fakeId = UUID.randomUUID();
    HttpEntity<UserRequest> requestUpdate = new HttpEntity<>(userRequest);

    ResponseEntity<UserResponse> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + fakeId, HttpMethod.PUT, requestUpdate, UserResponse.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void willDeleteUser() {
    UserRequest userRequest = createTestUserRequest();
    ResponseEntity<UserResponse> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponse.class);
    UserResponse createdUser = userResponseEntity.getBody();

    ResponseEntity<HttpStatus> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + createdUser.getId(),
            HttpMethod.DELETE,
            null,
            HttpStatus.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    ResponseEntity<String> getUserResponse =
        this.testRestTemplate.getForEntity(
            getBaseUserUrl() + "/" + createdUser.getId(), String.class);
    assertEquals(HttpStatus.NOT_FOUND, getUserResponse.getStatusCode());
  }

  @Test
  void willNotDeleteNonexistentUser() {
    UUID fakeId = UUID.randomUUID();

    ResponseEntity<Void> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + fakeId, HttpMethod.DELETE, null, Void.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  private UserResponse sendUserCreateRequest(UserRequest userRequest) {
    ResponseEntity<UserResponse> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponse.class);
    UserResponse createdUser = userResponseEntity.getBody();
    assertNotNull(createdUser);
    return createdUser;
  }
}
