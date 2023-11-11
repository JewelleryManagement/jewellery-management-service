package jewellery.inventory.integration;

import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SystemEventCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  @Autowired SystemEventRepository systemEventRepository;

  private String getBaseSystemEventUrl() {
    return BASE_URL_PATH + port + "/system-events";
  }

  @BeforeEach
  void cleanup() {
    systemEventRepository.deleteAll();
  }

  @Test
  void willGetAllSystemEvents() {
    createAndSaveUser();

    ResponseEntity<List<SystemEvent>> eventResponse =
        this.testRestTemplate.exchange(
            getBaseSystemEventUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    List<SystemEvent> events = eventResponse.getBody();
    assertFalse(events != null && events.isEmpty());
    assertNotNull(events, "Retrieved events list is empty");

    Map<String, Object> payload = events.get(0).getPayload();
    Object eventType = events.get(0).getType();

    assertEquals(EventType.USER_CREATE, eventType);
    assertTrue(payload.containsKey("entity"));
    assertEquals("john", ((Map<?, ?>) payload.get("entity")).get("name"));
  }

  private void createAndSaveUser() {
    UserRequestDto userRequest = createTestUserRequest();

    this.testRestTemplate.postForEntity(
        BASE_URL_PATH + port + "/users", userRequest, UserResponseDto.class);

    ResponseEntity<List<UserResponseDto>> response =
        this.testRestTemplate.exchange(
            BASE_URL_PATH + port + "/users",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<UserResponseDto> users = response.getBody();
    assertNotNull(users);
  }
}
