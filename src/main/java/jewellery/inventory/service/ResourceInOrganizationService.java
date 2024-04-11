package jewellery.inventory.service;

import static jewellery.inventory.model.EventType.ORGANIZATION_REMOVE_RESOURCE_QUANTITY;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.response.OrganizationTransferResourceResponseDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.not_found.ResourceInOrganizationNotFoundException;
import jewellery.inventory.mapper.ResourceInOrganizationMapper;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.OrganizationPermission;
import jewellery.inventory.model.ResourceInOrganization;
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
  private final OrganizationService organizationService;
  private final ResourceService resourceService;
  private final ResourceInOrganizationMapper resourceInOrganizationMapper;
  private static final BigDecimal EPSILON = new BigDecimal("1e-10");

  @Transactional
  @LogCreateEvent(eventType = EventType.ORGANIZATION_RESOURCE_TRANSFER)
  public OrganizationTransferResourceResponseDto transferResource(
      TransferResourceRequestDto transferResourceRequestDto) {
    Organization previousOwner =
        organizationService.getOrganization(transferResourceRequestDto.getPreviousOwnerId());

    organizationService.validateCurrentUserPermission(
        previousOwner, OrganizationPermission.TRANSFER_RESOURCE);

    Organization newOwner =
        organizationService.getOrganization(transferResourceRequestDto.getNewOwnerId());

    ResourceInOrganization resourceInPreviousOwner =
        findResourceInOrganizationOrThrow(
            previousOwner, transferResourceRequestDto.getTransferredResourceId());

    removeQuantityFromResource(resourceInPreviousOwner, transferResourceRequestDto.getQuantity());

    addResourceToOrganization(
        newOwner,
        resourceInPreviousOwner.getResource(),
        transferResourceRequestDto.getQuantity(),
        BigDecimal.ZERO);

    OrganizationTransferResourceResponseDto transferResourceResponseDto =
        resourceInOrganizationMapper.getOrganizationTransferResourceResponseDto(
            previousOwner,
            newOwner,
            resourceInPreviousOwner.getResource(),
            transferResourceRequestDto.getQuantity());
    logger.info("Transfer completed successfully {}", transferResourceResponseDto);

    return transferResourceResponseDto;
  }

  @LogUpdateEvent(eventType = EventType.ORGANIZATION_ADD_RESOURCE_QUANTITY)
  @Transactional
  public ResourcesInOrganizationResponseDto addResourceToOrganization(
      ResourceInOrganizationRequestDto resourceInOrganizationRequestDto) {
    Organization organization =
        organizationService.getOrganization(resourceInOrganizationRequestDto.getOrganizationId());

    organizationService.validateCurrentUserPermission(
        organization, OrganizationPermission.ADD_RESOURCE_QUANTITY);

    Resource resource =
        resourceService.getResourceById(resourceInOrganizationRequestDto.getResourceId());

    ResourceInOrganization resourceInOrganization =
        addResourceToOrganization(
            organization,
            resource,
            resourceInOrganizationRequestDto.getQuantity(),
            resourceInOrganizationRequestDto.getDealPrice());

    return resourceInOrganizationMapper.toResourcesInOrganizationResponse(resourceInOrganization);
  }

  @Transactional
  @LogUpdateEvent(eventType = ORGANIZATION_REMOVE_RESOURCE_QUANTITY)
  public ResourcesInOrganizationResponseDto removeQuantityFromResource(
      UUID organizationId, UUID resourceId, BigDecimal quantity) {
    return removeQuantityFromResourceNoLog(organizationId, resourceId, quantity);
  }

  public ResourcesInOrganizationResponseDto getAllResourcesFromOrganization(UUID organizationId) {
    Organization organization = organizationService.getOrganization(organizationId);
    organizationService.validateUserInOrganization(organization);

    return resourceInOrganizationMapper.toResourcesInOrganizationResponse(organization);
  }

  private ResourcesInOrganizationResponseDto removeQuantityFromResourceNoLog(
      UUID organizationId, UUID resourceId, BigDecimal quantity) {
    Organization organization = organizationService.getOrganization(organizationId);

    organizationService.validateCurrentUserPermission(
        organization, OrganizationPermission.REMOVE_RESOURCE_QUANTITY);

    ResourceInOrganization resourceInOrganization =
        findResourceInOrganizationOrThrow(organization, resourceId);
    removeQuantityFromResource(resourceInOrganization, quantity);

    return resourceInOrganizationMapper.toResourcesInOrganizationResponse(resourceInOrganization);
  }

  private void removeQuantityFromResource(
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
    logger.debug("ResourceInOrganization after quantity removal: {}", resourceInOrganization);
  }

  public ResourceInOrganization findResourceInOrganizationOrThrow(
      Organization previousOwner, UUID resourceId) {
    return findResourceInOrganization(previousOwner, resourceId)
        .orElseThrow(
            () -> new ResourceInOrganizationNotFoundException(resourceId, previousOwner.getId()));
  }

  private boolean isApproachingZero(BigDecimal value) {
    return value.abs().compareTo(EPSILON) < 0;
  }

  private boolean isNegative(BigDecimal value) {
    return value.compareTo(BigDecimal.ZERO) < 0;
  }

  private Optional<ResourceInOrganization> findResourceInOrganization(
      Organization organization, UUID resourceId) {
    logger.debug(
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
        "Getting resource in organization. Organization: {}, Resource: {}", organization, resource);
    return findResourceInOrganization(organization, resource.getId())
        .orElseGet(
            () -> createAndAddNewResourceInOrganization(organization, resource, BigDecimal.ZERO));
  }

  public ResourceInOrganization addResourceToOrganization(
      Organization organization, Resource resource, BigDecimal quantity, BigDecimal dealPrice) {
    logger.info(
        "Adding resource to organization. Organization: {}, Resource: {}, Quantity: {}",
        organization,
        resource,
        quantity);
    ResourceInOrganization resourceInOrganization =
        getResourceInOrganization(organization, resource);
    resourceInOrganization.setQuantity(resourceInOrganization.getQuantity().add(quantity));
    resourceInOrganization.setDealPrice(dealPrice);
    resourceInOrganizationRepository.save(resourceInOrganization);
    logger.debug("ResourceInOrganization after addition: {}", resource);
    return resourceInOrganization;
  }

  private ResourcesInOrganizationResponseDto getResourceInOrganizationResponse(
      UUID organizationId, UUID resourceId) {
    ResourceInOrganization resourceInOrganization =
        getResourceInOrganization(organizationId, resourceId);
    return resourceInOrganizationMapper.toResourcesInOrganizationResponse(resourceInOrganization);
  }

  private ResourceInOrganization getResourceInOrganization(UUID organizationId, UUID resourceId) {
    return findResourceInOrganization(
            organizationService.getOrganization(organizationId), resourceId)
        .orElse(null);
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
}
