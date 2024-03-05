package jewellery.inventory.service;

import static jewellery.inventory.model.EventType.ORGANIZATION_REMOVE_RESOURCE;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.not_found.ResourceInUserNotFoundException;
import jewellery.inventory.mapper.ResourceInOrganizationMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceInOrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceInOrganizationService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(ResourceInOrganizationService.class);
  private final ResourceInOrganizationRepository resourceInOrganizationRepository;
  private final UserInOrganizationService userInOrganizationService;
  private final OrganizationService organizationService;
  private final ResourceService resourceService;
  private final ResourceInOrganizationMapper resourceInOrganizationMapper;

  private static final BigDecimal EPSILON = new BigDecimal("1e-10");

  @LogUpdateEvent(eventType = EventType.ORGANIZATION_ADD_RESOURCE)
  @Transactional
  public ResourcesInOrganizationResponseDto addResourceToOrganization(
      ResourceInOrganizationRequestDto resourceInOrganizationRequestDto) {
    Organization organization =
        organizationService.getOrganization(resourceInOrganizationRequestDto.getOrganizationId());
    userInOrganizationService.validateCurrentUserPermission(
        organization, OrganizationPermission.ADD_RESOURCE_QUANTITY);

    Resource resource =
        resourceService.getResourceById(resourceInOrganizationRequestDto.getResourceId());

    ResourceInOrganization resourceInOrganization =
        addResourceToOrganization(
            organization, resource, resourceInOrganizationRequestDto.getQuantity());

    return resourceInOrganizationMapper.toResourceInOrganizationResponse(resourceInOrganization);
  }

  @Transactional
  @LogUpdateEvent(eventType = ORGANIZATION_REMOVE_RESOURCE)
  public ResourcesInOrganizationResponseDto removeQuantityFromResource(
      UUID organizationId, UUID resourceId, BigDecimal quantity) {
    return removeQuantityFromResourceNoLog(organizationId, resourceId, quantity);
  }

  public ResourcesInOrganizationResponseDto removeQuantityFromResourceNoLog(
      UUID organizationId, UUID resourceId, BigDecimal quantity) {
    Organization organization = organizationService.getOrganization(organizationId);
    userInOrganizationService.validateCurrentUserPermission(
        organization, OrganizationPermission.REMOVE_RESOURCE_QUANTITY);

    ResourceInOrganization resourceInOrganization =
        findResourceInOrganizationOrThrow(organization, resourceId);
    removeQuantityFromResource(resourceInOrganization, quantity);

    if (resourceInOrganization != null) {
      return resourceInOrganizationMapper.toResourceInOrganizationResponse(resourceInOrganization);
    }
    return new ResourcesInOrganizationResponseDto();
  }

  public void removeQuantityFromResource(
      ResourceInOrganization resourceInOrganization, BigDecimal quantityToRemove) {

    BigDecimal totalQuantity = resourceInOrganization.getQuantity();
    BigDecimal newQuantity = totalQuantity.subtract(quantityToRemove);

    if (isNegative(newQuantity)) {
      throw new InsufficientResourceQuantityException(quantityToRemove, totalQuantity);
    }

    resourceInOrganization.setQuantity(newQuantity);
    Organization organization = resourceInOrganization.getOrganization();
    if (isApproachingZero(newQuantity)) {
      organization.getResourceInOrganization().remove(resourceInOrganization);
      resourceInOrganization = null;
    }
    organizationService.saveOrganization(organization);
    logger.debug("ResourceInUser after quantity removal: {}", resourceInOrganization);
  }

  public ResourceInOrganization findResourceInOrganizationOrThrow(
      Organization previousOwner, UUID resourceId) {
    return findResourceInOrganization(previousOwner, resourceId)
        .orElseThrow(() -> new ResourceInUserNotFoundException(resourceId, previousOwner.getId()));
  }

  @Override
  public Object fetchEntity(Object... ids) {
    if (ids != null && ids.length > 0) {
      if (ids[0] instanceof ResourceInOrganizationRequestDto resourceInOrganizationRequestDto) {

        return getResourceInOrganizationResponse(
            resourceInOrganizationRequestDto.getOrganizationId(),
            resourceInOrganizationRequestDto.getResourceId());
      } else {
        return getResourceInOrganizationResponse((UUID) ids[0], (UUID) ids[1]);
      }
    }
    return null;
  }

  public ResourcesInOrganizationResponseDto getAllResourcesFromOrganization(UUID organizationId) {
    Organization organization = organizationService.getOrganization(organizationId);
    userInOrganizationService.validateUserInOrganization(organization);

    return resourceInOrganizationMapper.toResourceInOrganizationResponseDto(organization);
  }

  private boolean isApproachingZero(BigDecimal value) {
    return value.abs().compareTo(EPSILON) < 0;
  }

  private boolean isNegative(BigDecimal value) {
    return value.compareTo(BigDecimal.ZERO) < 0;
  }

  private Optional<ResourceInOrganization> findResourceInOrganization(
      Organization organization, UUID resourceId) {
    logger.info(
        "Finding resource by ID: {}, Finding organization by ID: {}",
        resourceId,
        organization.getId());
    return organization.getResourceInOrganization().stream()
        .filter(resource -> resource.getResource().getId().equals(resourceId))
        .findFirst();
  }

  private ResourceInOrganization createAndAddNewResourceInOrganization(
      Organization organization, Resource resource, BigDecimal quantity) {
    ResourceInOrganization resourceInOrganization =
        ResourceInOrganization.builder()
            .organization(organization)
            .resource(resource)
            .quantity(quantity)
            .build();
    logger.info("New resource in organization created: {}", resourceInOrganization);
    return resourceInOrganization;
  }

  private ResourceInOrganization getResourceInOrganization(
      Organization organization, Resource resource) {
    logger.debug(
        "Getting resource in user. Organization: {}, Resource: {}", organization, resource);
    return findResourceInOrganization(organization, resource.getId())
        .orElseGet(
            () -> createAndAddNewResourceInOrganization(organization, resource, BigDecimal.ZERO));
  }

  private ResourceInOrganization addResourceToOrganization(
      Organization organization, Resource resource, BigDecimal quantity) {
    logger.info(
        "Adding resource to user. Organization: {}, Resource: {}, Quantity: {}",
        organization,
        resource,
        quantity);
    ResourceInOrganization resourceInOrganization =
        getResourceInOrganization(organization, resource);
    resourceInOrganization.setQuantity(resourceInOrganization.getQuantity().add(quantity));
    resourceInOrganizationRepository.save(resourceInOrganization);
    logger.debug("ResourceInOrganization after addition: {}", resource);
    return resourceInOrganization;
  }

  private ResourcesInOrganizationResponseDto getResourceInOrganizationResponse(
      UUID organizationId, UUID resourceId) {
    Organization organization = organizationService.getOrganization(organizationId);
    Resource resource = resourceService.getResourceById(resourceId);
    ResourceInOrganization resourceInOrganization =
        getResourceInOrganization(organization, resource);
    if (resourceInOrganization != null) {
      return resourceInOrganizationMapper.toResourceInOrganizationResponseDto(organization);
    }
    return null;
  }
}
