package jewellery.inventory.integration;

import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.helper.SystemEventTestHelper.getUpdateEventPayload;
import static jewellery.inventory.helper.UserTestHelper.createDifferentUserRequest;
import static jewellery.inventory.helper.UserTestHelper.createInvalidUserRequest;
import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static jewellery.inventory.model.EventType.USER_CREATE;
import static jewellery.inventory.model.EventType.USER_DELETE;
import static jewellery.inventory.model.EventType.USER_UPDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.DetailedUserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UserCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

  private String getBaseUserUrl() {
    return "/users";
  }

  @Test
  void createUserSuccessfully() throws Exception {

    UserRequestDto userRequest = createTestUserRequest();

    ResponseEntity<DetailedUserResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseUserUrl(), userRequest, DetailedUserResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    DetailedUserResponseDto userResponse = response.getBody();

    assertNotNull(userResponse);
    assertNotNull(userResponse.getId());
    assertEquals(userRequest.getFirstName(), userResponse.getFirstName());
    assertEquals(userRequest.getEmail(), userResponse.getEmail());
    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(userResponse, objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        USER_CREATE, expectedEventPayload, response.getBody().getId());
  }

  @Test
  void createUserFailsWhenNameInvalid() {
    assertTrue(
        this.testRestTemplate
            .postForEntity(
                getBaseUserUrl(), createInvalidUserRequest(), DetailedUserResponseDto.class)
            .getStatusCode()
            .is4xxClientError());
  }

  @Test
  void createUserFailsWhenEmailDuplicate() {
    UserRequestDto userRequest = createTestUserRequest();

    this.testRestTemplate.postForEntity(
        getBaseUserUrl(), userRequest, DetailedUserResponseDto.class);

    ResponseEntity<DetailedUserResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseUserUrl(), userRequest, DetailedUserResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createUserFailsWhenNameDuplicate() {
    UserRequestDto userRequest = createTestUserRequest();

    this.testRestTemplate.postForEntity(
        getBaseUserUrl(), userRequest, DetailedUserResponseDto.class);

    UserRequestDto duplicateNameUserRequest = new UserRequestDto();
    duplicateNameUserRequest.setFirstName(userRequest.getFirstName());
    duplicateNameUserRequest.setEmail("differentEmail@example.com");

    ResponseEntity<DetailedUserResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseUserUrl(), duplicateNameUserRequest, DetailedUserResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void getAllUsersSuccessfully() {
    UserRequestDto userRequest = createTestUserRequest();

    ResponseEntity<DetailedUserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseUserUrl(), userRequest, DetailedUserResponseDto.class);
    assertNotNull(userResponseEntity.getBody());

    ResponseEntity<List<DetailedUserResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<DetailedUserResponseDto> users = response.getBody();
    assertNotNull(users);
    assertFalse(users.isEmpty());
    assertEquals(userRequest.getFirstName(), users.get(1).getFirstName());
    assertEquals(userRequest.getEmail(), users.get(1).getEmail());
  }

  @Test
  void getSpecificUserSuccessfully() {
    UserRequestDto userRequest = createTestUserRequest();
    ResponseEntity<DetailedUserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseUserUrl(), userRequest, DetailedUserResponseDto.class);
    DetailedUserResponseDto createdUser = userResponseEntity.getBody();

    ResponseEntity<DetailedUserResponseDto> response =
        this.testRestTemplate.getForEntity(
            getBaseUserUrl() + "/" + createdUser.getId(), DetailedUserResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    DetailedUserResponseDto fetchedUser = response.getBody();

    assertNotNull(fetchedUser);
    assertEquals(createdUser.getId(), fetchedUser.getId());
    assertEquals(userRequest.getFirstName(), fetchedUser.getFirstName());
    assertEquals(userRequest.getEmail(), fetchedUser.getEmail());
  }

  @Test
  void getUserFailsWhenUserNotFound() {
    UUID randomId = UUID.randomUUID();
    ResponseEntity<String> response =
        this.testRestTemplate.getForEntity(getBaseUserUrl() + "/" + randomId, String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void updateUserSuccessfully() throws JsonProcessingException {
    UserRequestDto userRequest = createTestUserRequest();
    ResponseEntity<DetailedUserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseUserUrl(), userRequest, DetailedUserResponseDto.class);
    DetailedUserResponseDto createdUser = userResponseEntity.getBody();

    UserRequestDto updatedUserRequest = createDifferentUserRequest();
    HttpEntity<UserRequestDto> requestUpdate = new HttpEntity<>(updatedUserRequest);

    ResponseEntity<DetailedUserResponseDto> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + createdUser.getId(),
            HttpMethod.PUT,
            requestUpdate,
            DetailedUserResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    DetailedUserResponseDto updatedUser = response.getBody();

    assertNotNull(updatedUser);
    assertEquals(createdUser.getId(), updatedUser.getId());
    assertEquals(updatedUserRequest.getFirstName(), updatedUser.getFirstName());
    assertEquals(updatedUserRequest.getEmail(), updatedUser.getEmail());

    Map<String, Object> expectedEventPayload =
        getUpdateEventPayload(createdUser, updatedUser, objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        USER_UPDATE, expectedEventPayload, response.getBody().getId());
  }

  @Test
  void updateUserFailsWhenEmailDuplicate() {

    DetailedUserResponseDto firstUser = sendUserCreateRequest(createTestUserRequest());

    DetailedUserResponseDto secondUser = sendUserCreateRequest(createDifferentUserRequest());

    UserRequestDto secondUserRequest = new UserRequestDto();
    secondUserRequest.setFirstName(secondUser.getFirstName());
    secondUserRequest.setEmail(firstUser.getEmail());

    HttpEntity<UserRequestDto> requestUpdate = new HttpEntity<>(secondUserRequest);
    ResponseEntity<DetailedUserResponseDto> responseEntity =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + secondUser.getId(),
            HttpMethod.PUT,
            requestUpdate,
            DetailedUserResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
  }

  @Test
  void updateUserFailsWhenUserNotFound() {
    UserRequestDto userRequest = createTestUserRequest();
    UUID fakeId = UUID.randomUUID();
    HttpEntity<UserRequestDto> requestUpdate = new HttpEntity<>(userRequest);

    ResponseEntity<DetailedUserResponseDto> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + fakeId,
            HttpMethod.PUT,
            requestUpdate,
            DetailedUserResponseDto.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void deleteUserSuccessfully() throws JsonProcessingException {
    UserRequestDto userRequest = createTestUserRequest();
    ResponseEntity<DetailedUserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseUserUrl(), userRequest, DetailedUserResponseDto.class);
    DetailedUserResponseDto createdUser = userResponseEntity.getBody();

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

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(createdUser, objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        USER_DELETE, expectedEventPayload, createdUser.getId());
  }

  @Test
  void deleteUserFailsWhenUserNotFound() {
    UUID fakeId = UUID.randomUUID();

    ResponseEntity<Void> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + fakeId, HttpMethod.DELETE, null, Void.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  private DetailedUserResponseDto sendUserCreateRequest(UserRequestDto userRequest) {
    ResponseEntity<DetailedUserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseUserUrl(), userRequest, DetailedUserResponseDto.class);
    DetailedUserResponseDto createdUser = userResponseEntity.getBody();
    assertNotNull(createdUser);
    return createdUser;
  }
}
