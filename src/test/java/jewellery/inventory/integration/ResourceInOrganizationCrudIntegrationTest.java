package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceTestHelper.getPearlRequestDto;
import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.helper.SystemEventTestHelper.getUpdateEventPayload;
import static jewellery.inventory.model.EventType.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.helper.ResourceInOrganizationTestHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ResourceInOrganizationCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

  private static final BigDecimal RESOURCE_QUANTITY = getBigDecimal("100");
  private static final BigDecimal RESOURCE_PRICE = getBigDecimal("105");
  private static final BigDecimal RESOURCE_QUANTITY_TO_REMOVE = getBigDecimal("5");

  private String buildUrl(String... paths) {
    return "/" + String.join("/", paths);
  }

  private String getBaseResourceAvailabilityUrl() {
    return buildUrl("organizations", "resources-availability");
  }

  private String getBaseOrganizationUrl() {
    return buildUrl("organizations");
  }

  private String getBaseResourceUrl() {
    return buildUrl("resources");
  }

  @Test
  void addResourceToOrganizationSuccessfully() throws JsonProcessingException {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();

    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        sendResourceToOrganization(resourceInOrganizationRequest);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    ResourcesInOrganizationResponseDto result = response.getBody();
    assertNotNull(result);
    assertEquals(1, result.getResourcesAndQuantities().size());
    assertEquals(organizationResponseDto.getId(), result.getOwner().getId());
    assertEquals(
        resourceResponse.getId(), result.getResourcesAndQuantities().get(0).getResource().getId());
    assertEquals(
        resourceInOrganizationRequest.getQuantity(),
        result.getResourcesAndQuantities().get(0).getQuantity());
    assertEquals(resourceInOrganizationRequest.getDealPrice(), result.getDealPrice());

    Map<String, Object> expectedEventPayload = getCreateOrDeleteEventPayload(result, objectMapper);
    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_ADD_RESOURCE_QUANTITY, expectedEventPayload);
  }

  @Test
  void addResourceToOrganizationShouldThrowWhenOrganizationNotFound() {

    ResourceResponseDto resourceResponse = createResourceResponse();

    ResourceInOrganizationRequestDto request =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            UUID.randomUUID(), resourceResponse.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        sendResourceToOrganization(request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addResourceToOrganizationShouldThrowWhenResourceNotFound() {
    OrganizationResponseDto organizationResponseDto = createOrganization();

    ResourceInOrganizationRequestDto request =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(), UUID.randomUUID(), RESOURCE_QUANTITY, RESOURCE_PRICE);

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        sendResourceToOrganization(request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addResourceToOrganizationShouldThrowWhenQuantityInvalid() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();

    ResourceInOrganizationRequestDto request =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            getBigDecimal(RESOURCE_QUANTITY.negate().toString()),
            getBigDecimal(RESOURCE_PRICE.negate().toString()));

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        sendResourceToOrganization(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void getAllResourcesFromOrganizationSuccessfully() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();
    ResourceResponseDto otherResource = createResourceResponse();

    ResourceInOrganizationRequestDto request =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);
    sendResourceToOrganization(request);

    ResourceInOrganizationRequestDto otherRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            otherResource.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);
    sendResourceToOrganization(otherRequest);

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        getAllResourcesByOrganizationId(organizationResponseDto);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getResourcesAndQuantities().size());
  }

  @Test
  void removeResourceQuantityFromOrganizationSuccessfully() throws JsonProcessingException {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();

    ResourceInOrganizationRequestDto request =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(), resourceResponse.getId(), RESOURCE_QUANTITY, null);

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        sendResourceToOrganization(request);
    ResponseEntity<ResourcesInOrganizationResponseDto> deletedQuantityResponse =
        sendDeleteOperation(getDeleteResourceUrl(organizationResponseDto, resourceResponse));

    assertEquals(
        RESOURCE_QUANTITY.subtract(RESOURCE_QUANTITY_TO_REMOVE),
        deletedQuantityResponse.getBody().getResourcesAndQuantities().get(0).getQuantity());

    Map<String, Object> expectedEventPayload =
        getUpdateEventPayload(response.getBody(), deletedQuantityResponse.getBody(), objectMapper);
    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_REMOVE_RESOURCE_QUANTITY, expectedEventPayload);
  }

  @Test
  void removeResourceFromDatabaseWhenResourceQuantityIsZero() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();

    ResourceInOrganizationRequestDto request =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY_TO_REMOVE,
            RESOURCE_PRICE);

    sendResourceToOrganization(request);
    ResponseEntity<ResourcesInOrganizationResponseDto> resourceResponseBeforeDeletingQuantity =
        getAllResourcesByOrganizationId(organizationResponseDto);
    assertEquals(
        1, resourceResponseBeforeDeletingQuantity.getBody().getResourcesAndQuantities().size());

    sendDeleteOperation(getDeleteResourceUrl(organizationResponseDto, resourceResponse));
    ResponseEntity<ResourcesInOrganizationResponseDto> resourceResponseAfterDeletingQuantity =
        getAllResourcesByOrganizationId(organizationResponseDto);

    assertEquals(
        0, resourceResponseAfterDeletingQuantity.getBody().getResourcesAndQuantities().size());
  }

  @Test
  void removeResourceQuantityShouldThrowWhenOrganizationNotFound() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();

    organizationResponseDto.setId(UUID.randomUUID());

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        sendDeleteOperation(getDeleteResourceUrl(organizationResponseDto, resourceResponse));
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityShouldThrowWhenResourceNotFound() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();

    resourceResponse.setId(UUID.randomUUID());

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        sendDeleteOperation(getDeleteResourceUrl(organizationResponseDto, resourceResponse));
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityShouldThrowWhenInsufficientQuantity() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();

    ResourceInOrganizationRequestDto request =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY_TO_REMOVE.subtract(getBigDecimal("1")),
            RESOURCE_PRICE);

    sendResourceToOrganization(request);

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        sendDeleteOperation(getDeleteResourceUrl(organizationResponseDto, resourceResponse));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void removeResourceShouldThrowWhenQuantityToRemoveIsNegative() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();

    ResourceInOrganizationRequestDto request =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);

    sendResourceToOrganization(request);

    String removeResourceURL =
        buildUrl(
            "organizations",
            "resources-availability",
            organizationResponseDto.getId().toString(),
            resourceResponse.getId().toString(),
            "-1");

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        sendDeleteOperation(removeResourceURL);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  private ResponseEntity<ResourcesInOrganizationResponseDto> getAllResourcesByOrganizationId(
      OrganizationResponseDto organizationResponseDto) {
    return this.testRestTemplate.getForEntity(
        getBaseResourceAvailabilityUrl() + "/" + organizationResponseDto.getId(),
        ResourcesInOrganizationResponseDto.class);
  }

  @NotNull
  private String getDeleteResourceUrl(
      OrganizationResponseDto organizationResponseDto, ResourceResponseDto resourceResponse) {
    return buildUrl(
        "organizations",
        "resources-availability",
        organizationResponseDto.getId().toString(),
        resourceResponse.getId().toString(),
        RESOURCE_QUANTITY_TO_REMOVE.toString());
  }

  private ResponseEntity<ResourcesInOrganizationResponseDto> sendDeleteOperation(
      String removeResourceUrl) {
    return this.testRestTemplate.exchange(
        removeResourceUrl,
        HttpMethod.DELETE,
        HttpEntity.EMPTY,
        ResourcesInOrganizationResponseDto.class);
  }

  private OrganizationResponseDto createOrganization() {
    OrganizationRequestDto organizationRequestDto =
        OrganizationTestHelper.getTestOrganizationRequest();
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationUrl(), organizationRequestDto, OrganizationResponseDto.class);

    OrganizationResponseDto organizationResponseDto = response.getBody();
    assertNotNull(organizationResponseDto);
    return organizationResponseDto;
  }

  private ResourceResponseDto createResourceResponse() {
    ResourceRequestDto resourceRequest = getPearlRequestDto();
    ResponseEntity<ResourceResponseDto> resourceResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseResourceUrl(), resourceRequest, ResourceResponseDto.class);

    assertEquals(HttpStatus.CREATED, resourceResponseEntity.getStatusCode());

    ResourceResponseDto createdResource = resourceResponseEntity.getBody();
    assertNotNull(createdResource);
    assertNotNull(createdResource.getId());
    return createdResource;
  }

  private ResponseEntity<ResourcesInOrganizationResponseDto> sendResourceToOrganization(
      ResourceInOrganizationRequestDto resourceInOrganizationRequest) {
    return this.testRestTemplate.postForEntity(
        getBaseResourceAvailabilityUrl(),
        resourceInOrganizationRequest,
        ResourcesInOrganizationResponseDto.class);
  }
}
