package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.data.util.StreamUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ResourceCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired private ResourceMapper resourceMapper;

  private String getBaseResourceUrl() {
    return BASE_URL_PATH + port + "/resources";
  }

  @AfterEach
  void deleteResources() throws JsonProcessingException {
    List<ResourceResponseDto> resourceDtosFromDb = getResourcesWithRequest();
    resourceDtosFromDb.forEach(
        resourceDTO -> testRestTemplate.delete(getBaseResourceUrl() + "/" + resourceDTO.getId()));
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
    ResponseEntity<String> response =
        testRestTemplate.getForEntity(getBaseResourceUrl() + "/" + UUID.randomUUID(), String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void willFailToUpdateResourceFromDatabaseWithWrongId() {
    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseResourceUrl() + "/" + UUID.randomUUID(),
            HttpMethod.PUT,
            new HttpEntity<>(getGemstoneResponseDto()),
            String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void willFailToDeleteResourceFromDatabaseWithWrongId() {
    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseResourceUrl() + "/" + UUID.randomUUID(),
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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

  private void assertInputMatchesFetchedFromServer(List<ResourceRequestDto> updatedInputDtos)
      throws JsonProcessingException {
    List<ResourceResponseDto> updatedResources = getResourcesWithRequest();
    updatedResources.forEach(resourceResponseDto -> resourceResponseDto.setId(null));

    assertEquals(
        updatedInputDtos.stream()
            .map(
                resourceRequestDto ->
                    resourceMapper.toResourceResponse(
                        resourceMapper.toResourceEntity(resourceRequestDto)))
            .toList(),
        updatedResources);
  }
}
