package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceTestHelper.*;
import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.helper.SystemEventTestHelper.getUpdateEventPayload;
import static jewellery.inventory.model.EventType.RESOURCE_CREATE;
import static jewellery.inventory.model.EventType.RESOURCE_DELETE;
import static jewellery.inventory.model.EventType.RESOURCE_UPDATE;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.mapper.ResourceMapper;
import org.hibernate.AssertionFailure;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.Pair;
import org.springframework.data.util.StreamUtils;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class ResourceCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  @Autowired private ResourceMapper resourceMapper;

  @NotNull
  private static List<UUID> getIds(List<ResourceResponseDto> dtos) {
    return dtos.stream().map(ResourceResponseDto::getId).toList();
  }

  private String getBaseResourceUrl() {
    return "/resources";
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

    List<ResourceResponseDto> createdResources = sendCreateRequestsFor(inputDtos);

    assertInputMatchesFetchedFromServer(inputDtos);

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(createdResources.get(0), objectMapper);

    systemEventTestHelper.assertEventWasLogged(RESOURCE_CREATE, expectedEventPayload);
  }

  @Test
  void willGetAResourceFromDatabase() throws JsonProcessingException {
    sendCreateRequestsFor(List.of(ResourceTestHelper.getDiamondRequestDto()));
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
          assertEquals(
              getBigDecimal("0"),
              resourceQuantityDto.getQuantity().setScale(2, RoundingMode.HALF_UP));
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
        sendCreateRequestsFor(List.of(ResourceTestHelper.getDiamondRequestDto()));

    ResourceQuantityResponseDto fetchedResourceQuantity =
        getResourceQuantityWithRequest(createdResources.get(0).getId());

    assertEquals(
        getBigDecimal("0"),
        fetchedResourceQuantity.getQuantity().setScale(2, RoundingMode.HALF_UP));
    assertEquals(
        createdResources.get(0).getPricePerQuantity(),
        fetchedResourceQuantity.getResource().getPricePerQuantity());
  }

  @Test
  void willUpdateResourceToDatabase() throws JsonProcessingException {
    sendCreateRequestsFor(provideResourceRequestDtos().toList());
    List<ResourceResponseDto> createdDtos = getResourcesWithRequest();
    List<ResourceRequestDto> updatedInputDtos = provideUpdatedResourceRequestDtos().toList();

    sendUpdateRequestsFor(updatedInputDtos, getIds(createdDtos));
    List<ResourceResponseDto> updatedDtos = getResourcesWithRequest();
    assertInputMatchesFetchedFromServer(updatedInputDtos);

    Map<String, Object> expectedEventPayload =
        getUpdateEventPayload(
            createdDtos.get(0),
            getMatchingUpdatedDto(createdDtos.get(0).getId(), updatedDtos),
            objectMapper);

    systemEventTestHelper.assertEventWasLogged(RESOURCE_UPDATE, expectedEventPayload);
  }

  @Test
  void willDeleteAResourceFromDatabase() throws JsonProcessingException {
    sendCreateRequestsFor(List.of(ResourceTestHelper.getDiamondRequestDto()));
    List<ResourceResponseDto> createdDtos = getResourcesWithRequest();

    testRestTemplate.delete(getBaseResourceUrl() + "/" + createdDtos.get(0).getId());

    List<ResourceResponseDto> afterDeleteDtos = getResourcesWithRequest();
    assertEquals(0, afterDeleteDtos.size());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(createdDtos.get(0), objectMapper);

    systemEventTestHelper.assertEventWasLogged(RESOURCE_DELETE, expectedEventPayload);
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
            new HttpEntity<>(getDiamondResponseDto()),
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

  @Test
  void willThrowWhenFileIsEmpty() {
    willImportCsvReturnsBadRequest(getEmptyTestFile());
  }

  @Test
  void willThrowWhenFileContentIsWrong() {
    willImportCsvReturnsBadRequest(getTestWrongContentFile());
  }

  @Test
  void willThrowWhenFileContentIsInInvalidFormat() {
    willImportCsvReturnsBadRequest(getTestInvalidFormatFile());
  }

  @Test
  void willImportResourcesSuccessfully() {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", getTestFile().getResource());
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    ParameterizedTypeReference<List<ResourceResponseDto>> responseType =
        new ParameterizedTypeReference<List<ResourceResponseDto>>() {};

    ResponseEntity<List<ResourceResponseDto>> response =
        testRestTemplate.exchange(getImportUrl(), HttpMethod.POST, requestEntity, responseType);

    List<ResourceResponseDto> responseDto = response.getBody();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(responseDto);
    assertEquals(responseDto.get(0).getClazz(), "Element");
    assertEquals(responseDto.get(0).getQuantityType(), "28");
    assertEquals(responseDto.get(0).getPricePerQuantity(), BigDecimal.valueOf(30));
    assertEquals(responseDto.get(0).getNote(), "smth");
    assertEquals(responseDto.get(0).getSku(), "S.K.U");
  }

  private void willImportCsvReturnsBadRequest(MockMultipartFile TestWrongContentFile) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", TestWrongContentFile.getResource());
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
        testRestTemplate.postForEntity(getImportUrl(), requestEntity, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  private MockMultipartFile getEmptyTestFile() {
    return new MockMultipartFile("file", "test-file.txt", "text/plain", "".getBytes());
  }

  private MockMultipartFile getTestWrongContentFile() {
    String data = "smth";
    return new MockMultipartFile("file", "test-file.txt", "text/plain", data.getBytes());
  }

  private MockMultipartFile getTestInvalidFormatFile() {
    String data =
        "\"clazz\",\"note\",\"pricePerQuantity\",\"quantityType\",\"description\",\"color\",\"purity\",\"type\",\"quality\",\"shape\",\"size\",\"carat\",\"clarity\",\"cut\",\"dimensionX\",\"dimensionY\",\"dimensionZ\"\n"
            + "Diamond,Note,invalid,unit,,ruby,,,,,octagon,,5.10,opaque,diamond,4.50,4.90,2.50";
    return new MockMultipartFile("file", "test-file.csv", "text/csv", data.getBytes());
  }

  private MockMultipartFile getTestFile() {
    String csvData =
        "clazz,quantityType,pricePerQuantity,note,description,sku\nElement,28,30,smth,Element description,S.K.U\n";
    return new MockMultipartFile("file", "test-file.csv", "text/csv", csvData.getBytes());
  }

  private String getImportUrl() {
    return getBaseResourceUrl() + "/" + "import";
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

    List<ResourceResponseDto> mappedDtos =
        updatedInputDtos.stream()
            .map(
                resourceRequestDto ->
                    resourceMapper.toResourceResponse(
                        resourceMapper.toResourceEntity(resourceRequestDto)))
            .toList();

    assertThat(mappedDtos).containsExactlyInAnyOrderElementsOf(updatedResources);
  }

  private ResourceResponseDto getMatchingUpdatedDto(
      UUID id, List<ResourceResponseDto> updatedDtos) {
    return updatedDtos.stream()
        .filter(resourceResponseDto -> resourceResponseDto.getId().equals(id))
        .findFirst()
        .orElseThrow(() -> new AssertionFailure("Can't find id: " + id + " in responses"));
  }
}
