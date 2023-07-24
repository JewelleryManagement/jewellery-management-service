package jewellery.inventory.integration;

import static jewellery.inventory.mapper.ResourceMapper.toResourceResponse;
import static jewellery.inventory.util.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.mapper.ResourceMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.util.Pair;
import org.springframework.data.util.StreamUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ResourceCrudIntegrationTest {

  public static final String INEXISTENT_ID = "/inexistent-id";

  private String getBaseResourceUrl() {
    return "http://localhost:" + port + "/resources";
  }

  @Value(value = "${local.server.port}")
  private int port;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired TestRestTemplate testRestTemplate;

  @AfterEach
  void deleteResources() throws JsonProcessingException {
    List<ResourceResponseDto> resourceDtosFromDb = getResourcesWithRequest();
    resourceDtosFromDb.forEach(
        resourceDTO -> testRestTemplate.delete(getBaseResourceUrl() + "/" + resourceDTO.getId()));
  }

  @Test
  void greetingShouldReturnDefaultMessage() {
    assertThat(
            this.testRestTemplate.getForObject("http://localhost:" + port + "/home", String.class))
        .contains("Hello world");
  }

  @Test
  void willAddResourceToDatabase() throws JsonProcessingException {
    List<ResourceRequestDto> inputDtos = provideResourceRequestDtos().toList();

    sendCreateRequestsFor(inputDtos);

    assertInputMatchesFetchedFromServer(inputDtos);
  }

  @Test
  void willUpdateResourceToDatabase() throws JsonProcessingException {
    sendCreateRequestsFor(provideResourceRequestDtos().toList());
    List<ResourceResponseDto> createdDtos = getResourcesWithRequest();
    List<ResourceRequestDto> updatedInputDtos = provideUpdatedResourceRequestDtos().toList();

    sendUpdateRequestsFor(updatedInputDtos, getIds(createdDtos));

    assertInputMatchesFetchedFromServer(updatedInputDtos);
  }

    @Test
    void willDeleteAResourceFromDatabase() throws JsonProcessingException {
      sendCreateRequestsFor(List.of(getGemstoneRequestDto()));
      List<ResourceResponseDto> createdDtos = getResourcesWithRequest();

      testRestTemplate.delete(getBaseResourceUrl() + "/" + createdDtos.get(0).getId());

      List<ResourceResponseDto> afterDeleteDtos = getResourcesWithRequest();
      assertEquals(0, afterDeleteDtos.size());
    }

    @Test
    void willFailToGetResourceFromDatabaseWithWrongId() {
      assertTrue(
          testRestTemplate
              .getForEntity(getBaseResourceUrl() + INEXISTENT_ID, String.class)
              .getStatusCode()
              .is4xxClientError());
    }

    @Test
    void willFailToUpdateResourceFromDatabaseWithWrongId() {
      ResponseEntity<String> response =
          testRestTemplate.exchange(
              getBaseResourceUrl() + INEXISTENT_ID,
              HttpMethod.PUT,
              new HttpEntity<>(getGemstoneResponseDto()),
              String.class);
      assertTrue(response.getStatusCode().is4xxClientError());
    }

  @Test
  void willFailToDeleteResourceFromDatabaseWithWrongId() {
    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseResourceUrl() + INEXISTENT_ID,
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            String.class);
    assertTrue(response.getStatusCode().is4xxClientError());
  }

  @NotNull
  private static List<UUID> getIds(List<ResourceResponseDto> dtos) {
    return dtos.stream().map(ResourceResponseDto::getId).toList();
  }

  @NotNull
  private List<ResourceResponseDto> getResourcesWithRequest() throws JsonProcessingException {
    String response = this.testRestTemplate.getForObject(getBaseResourceUrl(), String.class);
    return objectMapper.readValue(response, new TypeReference<>() {});
  }

  private void sendCreateRequestsFor(List<ResourceRequestDto> inputDtos) {
    inputDtos.forEach(
        resourceDTO -> {
          ResponseEntity<ResourceResponseDto> responseEntity =
              this.testRestTemplate.postForEntity(
                  getBaseResourceUrl(), resourceDTO, ResourceResponseDto.class);
          assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        });
  }

  private void sendUpdateRequestsFor(
      List<ResourceRequestDto> updatedResourceDtosWithNullIds, List<UUID> ids) {
    List<Pair<ResourceRequestDto, UUID>> dtosAndIds =
        createPairs(updatedResourceDtosWithNullIds, ids);
    dtosAndIds.forEach(
        requestDtoUUIDPair ->
            this.testRestTemplate.put(
                getBaseResourceUrl() + "/" + requestDtoUUIDPair.getSecond(),
                requestDtoUUIDPair.getFirst(),
                ResourceResponseDto.class));
  }

  @NotNull
  private List<Pair<ResourceRequestDto, UUID>> createPairs(
      List<ResourceRequestDto> resourceRequestDtos, List<UUID> ids) {
    return StreamUtils.zip(resourceRequestDtos.stream(), ids.stream(), Pair::of).toList();
  }

  private void assertInputMatchesFetchedFromServer(List<ResourceRequestDto> updatedInputDtos) throws JsonProcessingException {
    List<ResourceResponseDto> updatedResources = getResourcesWithRequest();
    updatedResources.forEach(resourceResponseDto -> resourceResponseDto.setId(null));
    assertEquals(
            updatedInputDtos.stream().map(resourceRequestDto -> toResourceResponse(ResourceMapper.toResourceEntity(resourceRequestDto))).toList(),
            updatedResources);
  }
}
