package jewellery.inventory.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.response.ResourceInOrganizationResponseDto;
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
public class ResourceInOrganizationService {
  private static final Logger logger = LogManager.getLogger(ResourceInOrganizationService.class);
  private final ResourceInOrganizationRepository resourceInOrganizationRepository;
  private final UserInOrganizationService userInOrganizationService;
  private final OrganizationService organizationService;
  private final ResourceService resourceService;
  private final ResourceInOrganizationMapper resourceInOrganizationMapper;

  @LogUpdateEvent(eventType = EventType.ORGANIZATION_ADD_RESOURCE)
  @Transactional
  public ResourceInOrganizationResponseDto addResourceToOrganization(
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

  private Optional<ResourceInOrganization> findResourceInOrganization(
      Organization organization, UUID resourceId) {
    logger.info(
        "Finding resource by ID: {}, Finding organization by ID: {}",
        resourceId,
        organization.getId());
    return organization.getResourceInOrganization().stream()
        .filter(resource -> resource.getOrganization().getId().equals(resourceId))
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
}
