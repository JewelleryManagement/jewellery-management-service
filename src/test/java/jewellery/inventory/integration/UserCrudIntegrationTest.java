package jewellery.inventory.integration;

import static jewellery.inventory.helper.SystemEventTestHelper.assertEventWasLogged;
import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.helper.SystemEventTestHelper.getEntityAsMap;
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
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.repository.SaleRepository;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UserCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  @Autowired UserRepository userRepository;
  @Autowired SystemEventRepository systemEventRepository;
  @Autowired SaleRepository saleRepository;
  @Autowired ProductRepository productRepository;

  private String getBaseUserUrl() {
    return BASE_URL_PATH + port + "/users";
  }

  @BeforeEach
  void cleanUp() {
    productRepository.deleteAll();
    saleRepository.deleteAll();
    userRepository.deleteAll();
    systemEventRepository.deleteAll();
  }

  private String getBaseSystemEventUrl() {
    return BASE_URL_PATH + port + "/system-events";
  }

  @Test
  void createUserSuccessfully() throws Exception {

    UserRequestDto userRequest = createTestUserRequest();

    ResponseEntity<UserResponseDto> response =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    UserResponseDto userResponse = response.getBody();

    assertNotNull(userResponse);
    assertNotNull(userResponse.getId());
    assertEquals(userRequest.getName(), userResponse.getName());
    assertEquals(userRequest.getEmail(), userResponse.getEmail());
    Map<String, Object> expectedEventSubPayload =
        getCreateOrDeleteEventPayload(getEntityAsMap(userResponse));

    assertEventWasLogged(
        this.testRestTemplate, getBaseSystemEventUrl(), USER_CREATE, expectedEventSubPayload);
  }

  @Test
  void createUserFailsWhenNameInvalid() {
    assertTrue(
        this.testRestTemplate
            .postForEntity(getBaseUserUrl(), createInvalidUserRequest(), UserResponseDto.class)
            .getStatusCode()
            .is4xxClientError());
  }

  @Test
  void createUserFailsWhenEmailDuplicate() {
    UserRequestDto userRequest = createTestUserRequest();

    this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);

    ResponseEntity<UserResponseDto> response =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createUserFailsWhenNameDuplicate() {
    UserRequestDto userRequest = createTestUserRequest();

    this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);

    UserRequestDto duplicateNameUserRequest = new UserRequestDto();
    duplicateNameUserRequest.setName(userRequest.getName());
    duplicateNameUserRequest.setEmail("differentEmail@example.com");

    ResponseEntity<UserResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseUserUrl(), duplicateNameUserRequest, UserResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void getAllUsersSuccessfully() {
    UserRequestDto userRequest = createTestUserRequest();

    ResponseEntity<UserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);
    assertNotNull(userResponseEntity.getBody());

    ResponseEntity<List<UserResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<UserResponseDto> users = response.getBody();
    assertNotNull(users);
    assertFalse(users.isEmpty());
    assertEquals(userRequest.getName(), users.get(0).getName());
    assertEquals(userRequest.getEmail(), users.get(0).getEmail());
  }

  @Test
  void getSpecificUserSuccessfully() {
    UserRequestDto userRequest = createTestUserRequest();
    ResponseEntity<UserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);
    UserResponseDto createdUser = userResponseEntity.getBody();

    ResponseEntity<UserResponseDto> response =
        this.testRestTemplate.getForEntity(
            getBaseUserUrl() + "/" + createdUser.getId(), UserResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    UserResponseDto fetchedUser = response.getBody();

    assertNotNull(fetchedUser);
    assertEquals(createdUser.getId(), fetchedUser.getId());
    assertEquals(userRequest.getName(), fetchedUser.getName());
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
    ResponseEntity<UserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);
    UserResponseDto createdUser = userResponseEntity.getBody();

    UserRequestDto updatedUserRequest = createDifferentUserRequest();
    HttpEntity<UserRequestDto> requestUpdate = new HttpEntity<>(updatedUserRequest);

    ResponseEntity<UserResponseDto> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + createdUser.getId(),
            HttpMethod.PUT,
            requestUpdate,
            UserResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    UserResponseDto updatedUser = response.getBody();

    assertNotNull(updatedUser);
    assertEquals(createdUser.getId(), updatedUser.getId());
    assertEquals(updatedUserRequest.getName(), updatedUser.getName());
    assertEquals(updatedUserRequest.getEmail(), updatedUser.getEmail());

    Map<String, Object> expectedEventSubPayload =
        getUpdateEventPayload(getEntityAsMap(createdUser), getEntityAsMap(updatedUser));

    assertEventWasLogged(
        this.testRestTemplate, getBaseSystemEventUrl(), USER_UPDATE, expectedEventSubPayload);
  }

  @Test
  void updateUserFailsWhenEmailDuplicate() {

    UserResponseDto firstUser = sendUserCreateRequest(createTestUserRequest());

    UserResponseDto secondUser = sendUserCreateRequest(createDifferentUserRequest());

    UserRequestDto secondUserRequest = new UserRequestDto();
    secondUserRequest.setName(secondUser.getName());
    secondUserRequest.setEmail(firstUser.getEmail());

    HttpEntity<UserRequestDto> requestUpdate = new HttpEntity<>(secondUserRequest);
    ResponseEntity<UserResponseDto> responseEntity =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + secondUser.getId(),
            HttpMethod.PUT,
            requestUpdate,
            UserResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
  }

  @Test
  void updateUserFailsWhenNameDuplicate() {
    UserResponseDto firstUser = sendUserCreateRequest(createTestUserRequest());
    UserResponseDto secondUser = sendUserCreateRequest(createDifferentUserRequest());

    UserRequestDto firstUserRequest = createTestUserRequest();
    UserRequestDto secondUserRequest = createDifferentUserRequest();

    secondUserRequest.setName(firstUserRequest.getName());
    HttpEntity<UserRequestDto> requestUpdate = new HttpEntity<>(secondUserRequest);
    ResponseEntity<UserResponseDto> responseEntity =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + secondUser.getId(),
            HttpMethod.PUT,
            requestUpdate,
            UserResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
  }

  @Test
  void updateUserFailsWhenUserNotFound() {
    UserRequestDto userRequest = createTestUserRequest();
    UUID fakeId = UUID.randomUUID();
    HttpEntity<UserRequestDto> requestUpdate = new HttpEntity<>(userRequest);

    ResponseEntity<UserResponseDto> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + fakeId, HttpMethod.PUT, requestUpdate, UserResponseDto.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void deleteUserSuccessfully() throws JsonProcessingException {
    UserRequestDto userRequest = createTestUserRequest();
    ResponseEntity<UserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);
    UserResponseDto createdUser = userResponseEntity.getBody();

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

    Map<String, Object> expectedEventSubPayload =
        getCreateOrDeleteEventPayload(getEntityAsMap(createdUser));

    assertEventWasLogged(
        this.testRestTemplate, getBaseSystemEventUrl(), USER_DELETE, expectedEventSubPayload);
  }

  @Test
  void deleteUserFailsWhenUserNotFound() {
    UUID fakeId = UUID.randomUUID();

    ResponseEntity<Void> response =
        this.testRestTemplate.exchange(
            getBaseUserUrl() + "/" + fakeId, HttpMethod.DELETE, null, Void.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  private UserResponseDto sendUserCreateRequest(UserRequestDto userRequest) {
    ResponseEntity<UserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);
    UserResponseDto createdUser = userResponseEntity.getBody();
    assertNotNull(createdUser);
    return createdUser;
  }
}
