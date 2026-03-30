package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganizationRequest;
import static jewellery.inventory.helper.ProductTestHelper.getProductRequestDtoForOrganization;
import static jewellery.inventory.helper.ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto;
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
import io.micrometer.common.lang.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.ProductsInOrganizationResponseDto;
import jewellery.inventory.dto.response.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.OrganizationRole;
import jewellery.inventory.model.resource.Diamond;
import jewellery.inventory.model.resource.Resource;
import org.hibernate.AssertionFailure;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
  private static final BigDecimal FIRST_QUANTITY = getBigDecimal("20");
  private static final BigDecimal SECOND_QUANTITY = getBigDecimal("10");
  private static final BigDecimal DEAL_PRICE = getBigDecimal("10");

  @Autowired private ResourceMapper resourceMapper;

  @NotNull
  private static List<UUID> getIds(List<ResourceResponseDto> dtos) {
    return dtos.stream().map(ResourceResponseDto::getId).toList();
  }

  private String buildUrl(String... paths) {
    return "/" + String.join("/", paths);
  }

  private String getBaseResourceUrl() {
    return "/resources";
  }

  private String getBaseOrganizationsUrl() {
    return "/organizations";
  }

  private String getBaseResourceInOrganizationAvailabilityUrl() {
    return buildUrl("organizations", "resources-availability");
  }

  private String getBaseProductUrl() {
    return "/products";
  }

  private OrganizationResponseDto organizationResponseDto;

  @BeforeEach
  void setUp() {
    OrganizationRequestDto organizationRequestDto = getTestOrganizationRequest();
    organizationResponseDto = createOrganizationsWithRequest(organizationRequestDto);
    OrganizationRole roleWithAllPermissions = createRoleWithAllPermissions();
    createOrganizationMembership(
        loggedInAdminUser.getId(), organizationResponseDto.getId(), roleWithAllPermissions.getId());
  }

  @AfterEach
  void deleteResources() throws JsonProcessingException {
    List<ResourceResponseDto> resourceDtosFromDb = getResourcesWithRequest();
    resourceDtosFromDb.forEach(resourceDTO -> deleteResourceById(resourceDTO.getId()));
  }

  @Test
  void willAddResourceToDatabase() throws JsonProcessingException {
    List<ResourceRequestDto> inputDtos = provideResourceRequestDtos().toList();

    List<ResourceResponseDto> createdResources = sendCreateRequestsFor(inputDtos);

    assertInputMatchesFetchedFromServer(inputDtos);

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(createdResources.get(0), objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        RESOURCE_CREATE, expectedEventPayload, createdResources.getFirst().getId());
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

    assertThat(resourceQuantityResponseDtos)
        .extracting(ResourceQuantityResponseDto::getResource)
        .containsExactlyInAnyOrderElementsOf(createdResources);
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

    systemEventTestHelper.assertEventWasLogged(
        RESOURCE_UPDATE, expectedEventPayload, updatedDtos.getFirst().getId());
  }

  @Test
  void willThrowWhenCreateResourceWithExistingSku() {
    ResourceRequestDto diamondRequestDto = ResourceTestHelper.getDiamondRequestDto();
    ResourceRequestDto pearlRequestDto = ResourceTestHelper.getPearlRequestDto();
    pearlRequestDto.setSku(diamondRequestDto.getSku());

    createResource(diamondRequestDto);
    ResponseEntity<String> response =
        this.testRestTemplate.postForEntity(getBaseResourceUrl(), pearlRequestDto, String.class);

    assetDuplicateSku(response, pearlRequestDto);
  }

  @Test
  void willThrowWhenUpdateResourceWithExistingSku() {
    ResourceRequestDto diamondRequestDto = ResourceTestHelper.getDiamondRequestDto();
    ResourceRequestDto pearlRequestDto = ResourceTestHelper.getPearlRequestDto();

    ResponseEntity<ResourceResponseDto> responseEntity1 = createResource(diamondRequestDto);
    createResource(pearlRequestDto);

    UUID resourceId = responseEntity1.getBody().getId();
    diamondRequestDto.setSku(pearlRequestDto.getSku());

    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseResourceUrl() + "/" + resourceId,
            HttpMethod.PUT,
            new HttpEntity<>(diamondRequestDto),
            String.class);

    assetDuplicateSku(response, diamondRequestDto);
  }

  @Test
  void willThrowWhenDeleteResourcePartOfOrganization() {
    Resource diamond = createDiamondInDatabase();
    createResourceInOrganization(diamond.getId());

    ResponseEntity<String> response = deleteResourceById(diamond.getId());

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Resource with id: " + diamond.getId() + " is part of organization!"));
  }

  @Test
  void willThrowWhenDeleteResourcePartOfProduct() {
    Resource diamond = createDiamondInDatabase();
    createResourceInOrganization(diamond.getId());
    createProductInOrganization(diamond.getId(), FIRST_QUANTITY);

    ResponseEntity<String> response = deleteResourceById(diamond.getId());

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Resource with id: " + diamond.getId() + " is part of product!"));
  }

  @Test
  void willThrowWhenDeleteResourcePartOfOrganizationAndProduct() {
    Resource diamond = createDiamondInDatabase();
    createResourceInOrganization(diamond.getId());
    createProductInOrganization(diamond.getId(), SECOND_QUANTITY);

    ResponseEntity<String> response = deleteResourceById(diamond.getId());

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains(
                "Resource with id: " + diamond.getId() + " is part of organization and product!"));
  }

  @Test
  void willDeleteAResourceFromDatabase() throws JsonProcessingException {
    sendCreateRequestsFor(List.of(ResourceTestHelper.getDiamondRequestDto()));
    List<ResourceResponseDto> createdDtos = getResourcesWithRequest();

    deleteResourceById(createdDtos.get(0).getId());

    List<ResourceResponseDto> afterDeleteDtos = getResourcesWithRequest();
    assertEquals(0, afterDeleteDtos.size());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(createdDtos.get(0), objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        RESOURCE_DELETE, expectedEventPayload, createdDtos.getFirst().getId());
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
    ResponseEntity<String> response = deleteResourceById(UUID.randomUUID());

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

  private List<ResourceResponseDto> sendCreateRequestsFor(List<ResourceRequestDto> inputDtos) {
    List<ResourceResponseDto> responses = new ArrayList<>();
    inputDtos.forEach(
        resourceDTO -> {
          ResponseEntity<ResourceResponseDto> responseEntity = createResource(resourceDTO);
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

  @Nullable
  private OrganizationResponseDto createOrganizationsWithRequest(OrganizationRequestDto dto) {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), dto, OrganizationResponseDto.class);

    return response.getBody();
  }

  @Nullable
  private Diamond createDiamondInDatabase() {
    ResourceRequestDto resourceRequest = ResourceTestHelper.getDiamondRequestDto();
    ResponseEntity<Diamond> createResource =
        this.testRestTemplate.postForEntity(getBaseResourceUrl(), resourceRequest, Diamond.class);

    return createResource.getBody();
  }

  private ResponseEntity<String> deleteResourceById(UUID id) {
    return testRestTemplate.exchange(
        getBaseResourceUrl() + "/" + id, HttpMethod.DELETE, null, String.class);
  }

  void createResourceInOrganization(UUID resourceId) {
    ResourceInOrganizationRequestDto resourceInOrganizationRequestDto =
        createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(), resourceId, FIRST_QUANTITY, DEAL_PRICE);

    testRestTemplate.postForEntity(
        getBaseResourceInOrganizationAvailabilityUrl(),
        resourceInOrganizationRequestDto,
        ResourcesInOrganizationResponseDto.class);
  }

  void createProductInOrganization(UUID resourceId, BigDecimal quantity) {
    ProductRequestDto productRequestDto =
        getProductRequestDtoForOrganization(
            loggedInAdminUser, organizationResponseDto.getId(), resourceId, quantity);

    testRestTemplate.postForEntity(
        getBaseProductUrl(), productRequestDto, ProductsInOrganizationResponseDto.class);
  }

  ResponseEntity<ResourceResponseDto> createResource(ResourceRequestDto resourceRequestDto) {
    return this.testRestTemplate.postForEntity(
        getBaseResourceUrl(), resourceRequestDto, ResourceResponseDto.class);
  }

  void assetDuplicateSku(ResponseEntity<String> response, ResourceRequestDto resourceRequestDto) {
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Stock Keeping Unit: " + resourceRequestDto.getSku() + " already exists!"));
  }
}
