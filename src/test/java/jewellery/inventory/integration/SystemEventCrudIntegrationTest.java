package jewellery.inventory.integration;

import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.http.ResponseEntity;

public class SystemEventCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired SystemEventRepository systemEventRepository;

  private String getBaseSystemEventUrl() {
    return BASE_URL_PATH + port + "/system-events";
  }

  @BeforeEach
  void setUp() {
    objectMapper.findAndRegisterModules();
    systemEventRepository.deleteAll();
  }

  @Test
  void willGetAllSystemEvents() {
    createAndSaveUser();

    ResponseEntity<Optional<List<SystemEvent>>> eventResponse =
        this.testRestTemplate.exchange(
            getBaseSystemEventUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    Optional<List<SystemEvent>> retrievedEvents = eventResponse.getBody();

    assertTrue(retrievedEvents.isPresent(), "No events retrieved");
    assertFalse(retrievedEvents.get().isEmpty(), "Retrieved events list is empty");
    assertNotNull(retrievedEvents);

    Map<String, Object> payload = retrievedEvents.get().get(0).getPayload();
    Object eventType = retrievedEvents.get().get(0).getType();

    assertEquals(EventType.USER_CREATE, eventType);
    assertTrue(payload.containsKey("entity"));
    assertEquals("john", ((Map<?, ?>) payload.get("entity")).get("name"));
  }

  private void createAndSaveUser() {
    UserRequestDto userRequest = createTestUserRequest();

    this.testRestTemplate.postForEntity(
        BASE_URL_PATH + port + "/users", userRequest, UserResponseDto.class);
  }
}
