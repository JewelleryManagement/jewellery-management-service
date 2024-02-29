package jewellery.inventory.integration;

import static jewellery.inventory.helper.SystemEventTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createDifferentTestUserRequest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

class SystemEventCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  @Test
  void willGetAllSystemEvents() throws JsonProcessingException {
    UserResponseDto userResponseDto = createAndSaveUser();

    ResponseEntity<List<SystemEvent>> eventResponse =
        this.testRestTemplate.exchange(
            getBaseSystemEventUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    List<SystemEvent> events = eventResponse.getBody();
    assertFalse(events != null && events.isEmpty());
    assertNotNull(events, "Retrieved events list is empty");

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(userResponseDto, objectMapper);

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(getBaseSystemEventUrl(), String.class);

    systemEventTestHelper.assertEventWasLogged(EventType.USER_CREATE, expectedEventPayload);
  }

  private String getBaseSystemEventUrl() {
    return "/system-events";
  }

  private UserResponseDto createAndSaveUser() {
    UserRequestDto userRequest = createDifferentTestUserRequest();

    return this.testRestTemplate
        .postForEntity("/users", userRequest, UserResponseDto.class)
        .getBody();
  }
}
