package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceTestHelper.getPreciousStoneRequestDto;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.dto.response.resource.PreciousStoneResponseDto;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.helper.ResourceInOrganizationTestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResourceInOrganizationCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

  private static final BigDecimal RESOURCE_QUANTITY = getBigDecimal("5");
  private static final BigDecimal RESOURCE_PRICE = getBigDecimal("105.5");

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
    PreciousStoneResponseDto resourceResponse = sendCreatePreciousStoneRequest();

    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);

    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        sendResourceToOrganization(resourceInOrganizationRequest);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());

    ResourcesInOrganizationResponseDto result = response.getBody();
    assertEquals(1, result.getResourcesAndQuantities().size());
    assertEquals(organizationResponseDto.getId(), result.getOwner().getId());

    //    Map<String, Object> expectedEventPayload =
    //            getCreateOrDeleteEventPayload(result, objectMapper);
    //
    //    systemEventTestHelper.assertEventWasLogged(ORGANIZATION_ADD_RESOURCE,
    // expectedEventPayload);
  }

  @Test
  void addResourceToOrganizationShouldThrowWhenOrganizationNotFound() {

    PreciousStoneResponseDto resourceResponse = sendCreatePreciousStoneRequest();

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
    PreciousStoneResponseDto resourceResponse = sendCreatePreciousStoneRequest();

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
    PreciousStoneResponseDto resourceResponse = sendCreatePreciousStoneRequest();
    PreciousStoneResponseDto otherResource = sendCreatePreciousStoneRequest();

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
        this.testRestTemplate.getForEntity(
            getBaseResourceAvailabilityUrl() + "/" + organizationResponseDto.getId(),
            ResourcesInOrganizationResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getResourcesAndQuantities().size());
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

  private PreciousStoneResponseDto sendCreatePreciousStoneRequest() {
    ResourceRequestDto resourceRequest = getPreciousStoneRequestDto();
    ResponseEntity<PreciousStoneResponseDto> resourceResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseResourceUrl(), resourceRequest, PreciousStoneResponseDto.class);

    assertEquals(HttpStatus.CREATED, resourceResponseEntity.getStatusCode());

    PreciousStoneResponseDto createdResource = resourceResponseEntity.getBody();
    assertNotNull(createdResource);
    assertNotNull(createdResource.getId());
    return createdResource;
  }

  private ResponseEntity<ResourcesInOrganizationResponseDto> sendResourceToOrganization(
      ResourceInOrganizationRequestDto resourceInOrganizationRequest) {
    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseResourceAvailabilityUrl(),
            resourceInOrganizationRequest,
            ResourcesInOrganizationResponseDto.class);
    return response;
  }
}
