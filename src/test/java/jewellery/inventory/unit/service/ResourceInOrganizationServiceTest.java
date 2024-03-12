package jewellery.inventory.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.organization.MissingOrganizationPermissionException;
import jewellery.inventory.exception.organization.UserIsNotPartOfOrganizationException;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.helper.ResourceInOrganizationTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.mapper.ResourceInOrganizationMapper;
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

  private Organization organization;
  private Resource resource;
  private ResourceInOrganizationRequestDto resourceInOrganizationRequestDto;
  private ResourceInOrganization resourceInOrganization;
  private static final BigDecimal QUANTITY = BigDecimal.ONE;
  private static final BigDecimal BIG_QUANTITY = BigDecimal.valueOf(30);
  private static final BigDecimal DEAL_PRICE = BigDecimal.TEN;

  @BeforeEach
  void setUp() {
    organization = OrganizationTestHelper.getTestOrganization();
    resource = ResourceTestHelper.getPearl();
    resourceInOrganizationRequestDto =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resource.getId(), QUANTITY, DEAL_PRICE);
    resourceInOrganization =
        ResourceInOrganizationTestHelper.createResourceInOrganization(organization, resource);
    organization.setResourceInOrganization(List.of(resourceInOrganization));
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
        .when(userInOrganizationService)
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
}
