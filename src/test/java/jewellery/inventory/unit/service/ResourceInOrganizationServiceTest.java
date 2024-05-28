package jewellery.inventory.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.response.ResourceOwnedByOrganizationsResponseDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.not_found.ResourceInOrganizationNotFoundException;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.exception.organization.MissingOrganizationPermissionException;
import jewellery.inventory.exception.organization.UserIsNotPartOfOrganizationException;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.helper.ResourceInOrganizationTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.mapper.ResourceInOrganizationMapper;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceInOrganizationRepository;
import jewellery.inventory.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceInOrganizationServiceTest {

  @InjectMocks private ResourceInOrganizationService resourceInOrganizationService;
  @Mock private ResourceInOrganizationRepository resourceInOrganizationRepository;
  @Mock private UserInOrganizationService userInOrganizationService;
  @Mock private OrganizationService organizationService;
  @Mock private ResourceService resourceService;
  @Mock private ResourceInOrganizationMapper resourceInOrganizationMapper;
  @Mock private OrganizationMapper organizationMapper;
  @Mock private ResourceMapper resourceMapper;

  private Organization organization;
  private Organization secondOrganization;
  private Resource resource;
  private Resource secondResource;
  private ResourceInOrganizationRequestDto resourceInOrganizationRequestDto;
  private ResourceInOrganization resourceInOrganization;
  private TransferResourceRequestDto transferResourceRequestDto;
  private static final BigDecimal QUANTITY = BigDecimal.ONE;
  private static final BigDecimal NEGATIVE_QUANTITY = BigDecimal.valueOf(-5);
  private static final BigDecimal BIG_QUANTITY = BigDecimal.valueOf(1000);
  private static final BigDecimal DEAL_PRICE = BigDecimal.TEN;

  @BeforeEach
  void setUp() {
    organization = OrganizationTestHelper.getTestOrganization();
    secondOrganization = OrganizationTestHelper.getTestOrganization();
    resource = ResourceTestHelper.getPearl();
    secondResource = ResourceTestHelper.getMetal();
    secondResource.setId(UUID.randomUUID());
    resourceInOrganizationRequestDto =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resource.getId(), QUANTITY, DEAL_PRICE);
    resourceInOrganization =
        ResourceInOrganizationTestHelper.createResourceInOrganization(organization, resource);
    organization.setResourceInOrganization(List.of(resourceInOrganization));
    secondOrganization.setResourceInOrganization(List.of(resourceInOrganization));
    transferResourceRequestDto =
        ResourceInOrganizationTestHelper.createTransferResourceRequestDto(
            organization.getId(),
            secondOrganization.getId(),
            resource.getId(),
            BigDecimal.valueOf(200));
  }

  @Test
  void testAddResourceToOrganizationShouldReturnNotFoundException() {
    when(organizationService.getOrganization(any())).thenThrow(OrganizationNotFoundException.class);

    Assertions.assertThrows(
        OrganizationNotFoundException.class,
        () ->
            resourceInOrganizationService.addResourceToOrganization(
                resourceInOrganizationRequestDto));
  }

  @Test
  void testAddResourceToOrganizationShouldThrowMissingOrganizationPermissionException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);

    doThrow(MissingOrganizationPermissionException.class)
        .when(organizationService)
        .validateCurrentUserPermission(organization, OrganizationPermission.ADD_RESOURCE_QUANTITY);

    Assertions.assertThrows(
        MissingOrganizationPermissionException.class,
        () ->
            resourceInOrganizationService.addResourceToOrganization(
                resourceInOrganizationRequestDto));
  }

  @Test
  void testAddResourceToOrganizationSuccessfully() {
    ResourcesInOrganizationResponseDto resourcesInOrganizationResponseDto =
        resourceInOrganizationMapper.toResourcesInOrganizationResponse(organization);
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(resourceService.getResourceById(resourceInOrganizationRequestDto.getResourceId()))
        .thenReturn(resource);
    when(resourceInOrganizationMapper.toResourcesInOrganizationResponse(resourceInOrganization))
        .thenReturn(resourcesInOrganizationResponseDto);

    resourceInOrganizationService.addResourceToOrganization(resourceInOrganizationRequestDto);

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(organizationService, times(1))
        .validateCurrentUserPermission(organization, OrganizationPermission.ADD_RESOURCE_QUANTITY);
    verify(resourceService, times(1))
        .getResourceById(resourceInOrganizationRequestDto.getResourceId());
    verify(resourceInOrganizationMapper, times(1))
        .toResourcesInOrganizationResponse(resourceInOrganization);
    verify(resourceInOrganizationRepository, times(1)).save(resourceInOrganization);
  }

  @Test
  void testRemoveQuantityFromResourceShouldThrowOrganizationNotFoundException() {
    when(organizationService.getOrganization(any())).thenThrow(OrganizationNotFoundException.class);

    Assertions.assertThrows(
        OrganizationNotFoundException.class,
        () ->
            resourceInOrganizationService.removeQuantityFromResource(
                organization.getId(), resource.getId(), QUANTITY));
  }

  @Test
  void testRemoveQuantityFromResourceShouldThrowMissingOrganizationPermissionException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);

    doThrow(MissingOrganizationPermissionException.class)
        .when(organizationService)
        .validateCurrentUserPermission(organization, OrganizationPermission.ADD_RESOURCE_QUANTITY);

    Assertions.assertThrows(
        MissingOrganizationPermissionException.class,
        () ->
            resourceInOrganizationService.addResourceToOrganization(
                resourceInOrganizationRequestDto));
  }

  @Test
  void testRemoveQuantityFromResourceShouldThrowInsufficientResourceQuantityException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    Assertions.assertThrows(
        InsufficientResourceQuantityException.class,
        () ->
            resourceInOrganizationService.removeQuantityFromResource(
                organization.getId(), resource.getId(), BIG_QUANTITY));
  }

  @Test
  void testRemoveQuantityFromResourceSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);

    resourceInOrganizationService.removeQuantityFromResource(
        organization.getId(), resource.getId(), QUANTITY);

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(organizationService, times(1)).saveOrganization(organization);
    verify(resourceInOrganizationMapper, times(1))
        .toResourcesInOrganizationResponse(resourceInOrganization);
  }

  @Test
  void testGetAllResourcesFromOrganizationShouldThrowOrganizationNotFoundException() {
    when(organizationService.getOrganization(any())).thenThrow(OrganizationNotFoundException.class);

    Assertions.assertThrows(
        OrganizationNotFoundException.class,
        () -> resourceInOrganizationService.getAllResourcesFromOrganization(organization.getId()));
  }

  @Test
  void testGetALLResourcesFromOrganizationShouldThrowUserIsNotPartOfOrganizationException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    doThrow(UserIsNotPartOfOrganizationException.class)
        .when(organizationService)
        .validateUserInOrganization(organization);

    Assertions.assertThrows(
        UserIsNotPartOfOrganizationException.class,
        () -> resourceInOrganizationService.getAllResourcesFromOrganization(organization.getId()));
  }

  @Test
  void testGetALLResourcesFromOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);

    resourceInOrganizationService.getAllResourcesFromOrganization(organization.getId());

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(resourceInOrganizationMapper, times(1)).toResourcesInOrganizationResponse(organization);
  }

  @Test
  void
      testTransferResourceShouldThrowOrganizationNotFoundExceptionWhenPreviousOwnerOrganizationDoesNotExists() {
    when(organizationService.getOrganization(organization.getId()))
        .thenThrow(OrganizationNotFoundException.class);

    Assertions.assertThrows(
        OrganizationNotFoundException.class,
        () -> resourceInOrganizationService.transferResource(transferResourceRequestDto));
  }

  @Test
  void
      testTransferResourceShouldThrowMissingOrganizationPermissionExceptionWhenUserHaveNoPermissionToTransfer() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    doThrow(MissingOrganizationPermissionException.class)
        .when(organizationService)
        .validateCurrentUserPermission(organization, OrganizationPermission.TRANSFER_RESOURCE);

    Assertions.assertThrows(
        MissingOrganizationPermissionException.class,
        () -> resourceInOrganizationService.transferResource(transferResourceRequestDto));
  }

  @Test
  void
      testTransferResourceShouldThrowOrganizationNotFoundExceptionWhenNewOwnerOrganizationDoesNotExists() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(organizationService.getOrganization(secondOrganization.getId()))
        .thenThrow(OrganizationNotFoundException.class);

    Assertions.assertThrows(
        OrganizationNotFoundException.class,
        () -> resourceInOrganizationService.transferResource(transferResourceRequestDto));
  }

  @Test
  void
      testTransferResourceShouldThrowResourceInOrganizationNotFoundExceptionWhenGivenResourceIsNotInPreviousOwner() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(organizationService.getOrganization(secondOrganization.getId()))
        .thenReturn(secondOrganization);

    transferResourceRequestDto.setTransferredResourceId(secondResource.getId());

    Assertions.assertThrows(
        ResourceInOrganizationNotFoundException.class,
        () -> resourceInOrganizationService.transferResource(transferResourceRequestDto));
  }

  @Test
  void
      testTransferResourceShouldThrowInsufficientResourceQuantityExceptionWhenQuantityToRemoveIsMoreThanOwned() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(organizationService.getOrganization(secondOrganization.getId()))
        .thenReturn(secondOrganization);

    Assertions.assertThrows(
        InsufficientResourceQuantityException.class,
        () -> resourceInOrganizationService.transferResource(transferResourceRequestDto));
  }

  @Test
  void testTransferResourceSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(organizationService.getOrganization(secondOrganization.getId()))
        .thenReturn(secondOrganization);

    transferResourceRequestDto.setQuantity(BigDecimal.valueOf(20));

    resourceInOrganizationService.transferResource(transferResourceRequestDto);

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(organizationService, times(1)).getOrganization(secondOrganization.getId());
    verify(organizationService, times(1)).saveOrganization(organization);
    verify(resourceInOrganizationRepository, times(1)).save(resourceInOrganization);
  }

  @Test
  void testGetOrganizationsAndQuantitiesSuccessfully() {
    when(resourceService.getResourceById(resource.getId())).thenReturn(resource);
    ResourceOwnedByOrganizationsResponseDto response =
        ResourceInOrganizationTestHelper.getResourceOwnedByOrganizationsResponseDto(organization);
    when(resourceInOrganizationService.getOrganizationsAndQuantities(resource.getId()))
        .thenReturn(response);

    ResourceOwnedByOrganizationsResponseDto actual =
        resourceInOrganizationService.getOrganizationsAndQuantities(resource.getId());

    Assertions.assertEquals(1, actual.getOrganizationsAndQuantities().size());
    Assertions.assertEquals(resource.getId(), actual.getResource().getId());
    Assertions.assertEquals(
        organization.getId(), actual.getOrganizationsAndQuantities().get(0).getOwner().getId());
    Assertions.assertEquals(
        BigDecimal.TEN, actual.getOrganizationsAndQuantities().get(0).getQuantity());
    verify(resourceInOrganizationMapper, times(1))
        .toResourcesOwnedByOrganizationsResponseDto(resource);
  }

  @Test
  void testGetOrganizationsAndQuantitiesShouldThrowWhenResourceNotFound() {
    when(resourceInOrganizationService.getOrganizationsAndQuantities(resource.getId()))
        .thenThrow(new ResourceNotFoundException(resource.getId()));

    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> resourceInOrganizationService.getOrganizationsAndQuantities(resource.getId()));
  }
}
