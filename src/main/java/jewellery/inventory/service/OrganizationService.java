package jewellery.inventory.service;

import java.util.*;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.organization.OrphanProductsInOrganizationException;
import jewellery.inventory.exception.organization.OrphanResourcesInOrganizationException;
import jewellery.inventory.exception.organization.UserIsNotPartOfOrganizationException;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.security.AuthService;
import jewellery.inventory.utils.NotUsedYet;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class OrganizationService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(OrganizationService.class);
  private static final String ORGANIZATION_ADMIN_ROLE_NAME = "ORGANIZATION_ADMIN";
  private final OrganizationRepository organizationRepository;
  private final OrganizationMapper organizationMapper;
  private final AuthService authService;
  private final UserService userService;
  private final ProductMapper productMapper;
  private final ScopedRoleService scopedRoleService;
  private final RoleMembershipRepository roleMembershipRepository;

  public List<OrganizationResponseDto> getAllOrganizationsResponsesForCurrentUser() {
    logger.debug("Fetching all organization responses for current user");

    UserResponseDto currentUser = authService.getCurrentUser();

    return organizationRepository
        .findOrganizationsByUserIdAndPermission(currentUser.getId(), Permission.ORGANIZATION_READ)
        .stream()
        .map(organizationMapper::toResponse)
        .toList();
  }

  public OrganizationResponseDto getOrganizationResponse(UUID id) {
    logger.debug("Get organizationResponse by ID: {}", id);
    return organizationMapper.toResponse(getOrganization(id));
  }

  public Organization saveOrganization(Organization organization) {
    return organizationRepository.save(organization);
  }

  @LogCreateEvent(eventType = EventType.ORGANIZATION_CREATE)
  @Transactional
  public OrganizationResponseDto create(OrganizationRequestDto organizationRequestDto) {
    Organization organization = organizationMapper.toEntity(organizationRequestDto);
    makeCurrentUserOwner(organization);
    organization = saveOrganization(organization);
    UserResponseDto currentUser = authService.getCurrentUser();
    ScopedRole adminRole = scopedRoleService.getRoleByName(ORGANIZATION_ADMIN_ROLE_NAME);
    roleMembershipRepository.insertAll(
        currentUser.getId(), organization.getId(), new UUID[] {adminRole.getId()});
    logger.info("Organization created with ID: {}", organization.getId());
    return organizationMapper.toResponse(organization);
  }

  @NotUsedYet(reason = "Pending frontend implementation")
  @LogDeleteEvent(eventType = EventType.ORGANIZATION_DELETE)
  public void delete(UUID organizationId) {
    Organization organizationForDelete = getOrganization(organizationId);

    verifyNoProductsOrResourcesInOrganization(organizationForDelete);
    organizationRepository.delete(organizationForDelete);
  }

  public Organization getOrganization(UUID id) {
    return organizationRepository
        .findById(id)
        .orElseThrow(() -> new OrganizationNotFoundException(id));
  }

  public void validateUserInOrganization(Organization organization) {
    User currentUser = userService.getUser(authService.getCurrentUser().getId());
    boolean isUserInOrganization =
        organization.getUsersInOrganization().stream()
            .anyMatch(userInOrganization -> userInOrganization.getUser().equals(currentUser));

    if (!isUserInOrganization) {
      throw new UserIsNotPartOfOrganizationException(currentUser.getId(), organization.getId());
    }
    logger.debug(
        "User permission validation successful. User ID: {}, Organization ID: {}",
        currentUser.getId(),
        organization.getId());
  }

  @Transactional(readOnly = true)
  public List<OrganizationResponseDto> getOrganizationsByPermission(Permission permission) {
    UUID currentUserId = authService.getCurrentUser().getId();

    return organizationRepository
        .findOrganizationsByUserIdAndPermission(currentUserId, permission)
        .stream()
        .map(organizationMapper::toResponse)
        .toList();
  }

  public ProductsInOrganizationResponseDto getProductsInOrganization(UUID organizationId) {
    Organization organization = getOrganization(organizationId);
    validateUserInOrganization(organization);

    return productMapper.mapToProductsInOrganizationResponseDto(
        organization, getProductsResponse(organization.getProductsOwned()));
  }

  private List<ProductResponseDto> getProductsResponse(List<Product> products) {
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  private void makeCurrentUserOwner(Organization organization) {
    UserInOrganization userInOrganizationOwner = new UserInOrganization();
    User user = userService.getUser(authService.getCurrentUser().getId());
    userInOrganizationOwner.setUser(user);
    userInOrganizationOwner.setOrganization(organization);
    organization.setUsersInOrganization(List.of(userInOrganizationOwner));
  }

  private void verifyNoProductsOrResourcesInOrganization(Organization organization) {
    if (!organization.getProductsOwned().isEmpty()) {
      throw new OrphanProductsInOrganizationException(organization.getId());
    }
    if (!organization.getResourceInOrganization().isEmpty()) {
      throw new OrphanResourcesInOrganizationException(organization.getId());
    }
  }

  @Override
  public Object fetchEntity(Object... ids) {
    Organization organization = organizationRepository.findById((UUID) ids[0]).orElse(null);
    if (organization == null) {
      return null;
    }
    return organizationMapper.toResponse(organization);
  }
}
