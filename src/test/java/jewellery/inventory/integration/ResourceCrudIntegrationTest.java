package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceTestHelper.*;
import static jewellery.inventory.helper.SystemEventTestHelper.assertEventWasLogged;
import static jewellery.inventory.model.EventType.RESOURCE_CREATE;
import static jewellery.inventory.model.EventType.RESOURCE_DELETE;
import static jewellery.inventory.model.EventType.RESOURCE_UPDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.repository.SystemEventRepository;
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
  @Autowired private SystemEventRepository systemEventRepository;

  private String getBaseResourceUrl() {
    return BASE_URL_PATH + port + "/resources";
  }

  private String getBaseSystemEventUrl() {
    return BASE_URL_PATH + port + "/system-events";
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
    assertEventWasLogged(
        this.testRestTemplate,
        getBaseSystemEventUrl(),
        RESOURCE_CREATE,
        "entity",
        "clazz",
        inputDtos.get(0).getClazz());
  }

  @Test
  void willGetAResourceFromDatabase() throws JsonProcessingException {
    sendCreateRequestsFor(List.of(ResourceTestHelper.getPreciousStoneRequestDto()));
    List<ResourceResponseDto> createdDtos = getResourcesWithRequest();

    testRestTemplate.getForEntity(
        getBaseResourceUrl() + "/" + createdDtos.get(0).getId(), ResourceResponseDto.class);

    List<ResourceResponseDto> resourcesList = getResourcesWithRequest();
    assertEquals(1, resourcesList.size());
  }

  @Test
  void willGetAllResourceQuantities() throws JsonProcessingException {
    List<ResourceResponseDto> createdResources =
        sendCreateRequestsFor(provideResourceRequestDtos().toList());

    List<ResourceQuantityResponseDto> resourceQuantityResponseDtos =
        getResourceQuantitiesWithRequest();

    resourceQuantityResponseDtos.forEach(
        resourceQuantityDto -> {
          assertEquals(0.0, resourceQuantityDto.getQuantity());
        });
    assertEquals(
        createdResources,
        resourceQuantityResponseDtos.stream()
            .map(ResourceQuantityResponseDto::getResource)
            .toList());
  }

  @Test
  void willGetSingleResourceQuantity() throws JsonProcessingException {
    List<ResourceResponseDto> createdResources =
        sendCreateRequestsFor(List.of(ResourceTestHelper.getPreciousStoneRequestDto()));

    ResourceQuantityResponseDto fetchedResourceQuantity =
        getResourceQuantityWithRequest(createdResources.get(0).getId());

    assertEquals(0.0, fetchedResourceQuantity.getQuantity());
    assertEquals(createdResources.get(0), fetchedResourceQuantity.getResource());
  }

  @Test
  void willUpdateResourceToDatabase() throws JsonProcessingException {
    sendCreateRequestsFor(provideResourceRequestDtos().toList());
    List<ResourceResponseDto> createdDtos = getResourcesWithRequest();
    List<ResourceRequestDto> updatedInputDtos = provideUpdatedResourceRequestDtos().toList();

    sendUpdateRequestsFor(updatedInputDtos, getIds(createdDtos));

    assertInputMatchesFetchedFromServer(updatedInputDtos);

    assertEventWasLogged(
        this.testRestTemplate,
        getBaseSystemEventUrl(),
        RESOURCE_UPDATE,
        "entityAfter",
        "clazz",
        updatedInputDtos.get(0).getClazz());
  }

  @Test
  void willDeleteAResourceFromDatabase() throws JsonProcessingException {
    sendCreateRequestsFor(List.of(ResourceTestHelper.getPreciousStoneRequestDto()));
    List<ResourceResponseDto> createdDtos = getResourcesWithRequest();

    testRestTemplate.delete(getBaseResourceUrl() + "/" + createdDtos.get(0).getId());

    List<ResourceResponseDto> afterDeleteDtos = getResourcesWithRequest();
    assertEquals(0, afterDeleteDtos.size());

    assertEventWasLogged(
        this.testRestTemplate,
        getBaseSystemEventUrl(),
        RESOURCE_DELETE,
        "entity",
        "clazz",
        createdDtos.get(0).getClazz());
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
            new HttpEntity<>(getPreciousStoneResponseDto()),
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

  @NotNull
  private List<ResourceQuantityResponseDto> getResourceQuantitiesWithRequest()
      throws JsonProcessingException {
    String response =
        this.testRestTemplate.getForObject(getBaseResourceUrl() + "/quantity", String.class);
    return objectMapper.readValue(response, new TypeReference<>() {});
  }

  @NotNull
  private ResourceQuantityResponseDto getResourceQuantityWithRequest(UUID resourceId)
      throws JsonProcessingException {
    String response =
        this.testRestTemplate.getForObject(
            getBaseResourceUrl() + "/quantity/" + resourceId, String.class);
    return objectMapper.readValue(response, new TypeReference<>() {});
  }

  private List<ResourceResponseDto> sendCreateRequestsFor(List<ResourceRequestDto> inputDtos) {
    List<ResourceResponseDto> responses = new ArrayList<>();
    inputDtos.forEach(
        resourceDTO -> {
          ResponseEntity<ResourceResponseDto> responseEntity =
              this.testRestTemplate.postForEntity(
                  getBaseResourceUrl(), resourceDTO, ResourceResponseDto.class);
          assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
          responses.add(responseEntity.getBody());
        });
    return responses;
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
