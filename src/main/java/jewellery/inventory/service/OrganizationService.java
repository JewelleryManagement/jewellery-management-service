package jewellery.inventory.service;

import java.util.*;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.organization.MissingOrganizationPermissionException;
import jewellery.inventory.exception.organization.OrphanProductsInOrganizationException;
import jewellery.inventory.exception.organization.OrphanResourcesInOrganizationException;
import jewellery.inventory.exception.organization.UserIsNotPartOfOrganizationException;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.security.AuthService;
import jewellery.inventory.utils.NotUsedYet;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrganizationService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(OrganizationService.class);
  private final OrganizationRepository organizationRepository;
  private final OrganizationMapper organizationMapper;
  private final AuthService authService;
  private final UserService userService;

  public List<OrganizationResponseDto> getAllOrganizationsResponsesForCurrentUser() {
    logger.debug("Fetching all organizationsResponses for current user ");
    return getOrganizationsUserIsPartOf().stream().map(organizationMapper::toResponse).toList();
  }

  @NotUsedYet(reason = "Pending frontend implementation")
  public OrganizationResponseDto getOrganizationResponse(UUID id) {
    logger.debug("Get organizationResponse by ID: {}", id);
    return organizationMapper.toResponse(getOrganization(id));
  }

  public Organization saveOrganization(Organization organization) {
    return organizationRepository.save(organization);
  }

  @LogCreateEvent(eventType = EventType.ORGANIZATION_CREATE)
  public OrganizationResponseDto create(OrganizationRequestDto organizationRequestDto) {
    Organization organization = organizationMapper.toEntity(organizationRequestDto);
    makeCurrentUserOwner(organization);
    organization = saveOrganization(organization);
    logger.info("Organization created with ID: {}", organization.getId());
    return organizationMapper.toResponse(organization);
  }

  @NotUsedYet(reason = "Pending frontend implementation")
  @LogDeleteEvent(eventType = EventType.ORGANIZATION_DELETE)
  public void delete(UUID organizationId) {
    Organization organizationForDelete = getOrganization(organizationId);
    validateCurrentUserPermission(
        organizationForDelete, OrganizationPermission.DESTROY_ORGANIZATION);
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

  public List<OrganizationResponseDto> getOrganizationsByPermission(
      OrganizationPermission organizationPermission) {
    User currentUser = userService.getUser(authService.getCurrentUser().getId());
    return getAll().stream()
        .filter(
            organization ->
                doesUserHavePermissionForOrganization(
                    organization, currentUser, organizationPermission))
        .map(organizationMapper::toResponse)
        .toList();
  }

  public void validateCurrentUserPermission(
      Organization organization, OrganizationPermission permission) {
    User currentUser = userService.getUser(authService.getCurrentUser().getId());
    if (!hasPermission(currentUser, organization, permission)) {
      throw new MissingOrganizationPermissionException(
          currentUser.getId(), organization.getId(), permission);
    }
    logger.debug(
        "User permission validation successful. User ID: {}, Organization ID: {}",
        currentUser.getId(),
        organization.getId());
  }

  private List<Organization> getOrganizationsUserIsPartOf() {
    User currentUser = userService.getUser(authService.getCurrentUser().getId());
    return getAll().stream()
        .filter(
            org ->
                org.getUsersInOrganization().stream()
                    .anyMatch(userOrg -> userOrg.getUser().equals(currentUser)))
        .toList();
  }

  private boolean doesUserHavePermissionForOrganization(
      Organization organization, User currentUser, OrganizationPermission organizationPermission) {
    return organization.getUsersInOrganization().stream()
        .anyMatch(
            userInOrganization ->
                userInOrganization.getUser().getId().equals(currentUser.getId())
                    && userInOrganization
                        .getOrganizationPermission()
                        .contains(organizationPermission));
  }

  private void makeCurrentUserOwner(Organization organization) {
    UserInOrganization userInOrganizationOwner = new UserInOrganization();
    User user = userService.getUser(authService.getCurrentUser().getId());
    userInOrganizationOwner.setUser(user);
    userInOrganizationOwner.setOrganization(organization);
    userInOrganizationOwner.setOrganizationPermission(
        Arrays.asList(OrganizationPermission.values()));
    organization.setUsersInOrganization(List.of(userInOrganizationOwner));
  }

  private List<Organization> getAll() {
    return organizationRepository.findAll();
  }

  private void verifyNoProductsOrResourcesInOrganization(Organization organization) {
    if (!organization.getProductsOwned().isEmpty()) {
      throw new OrphanProductsInOrganizationException(organization.getId());
    }
    if (!organization.getResourceInOrganization().isEmpty()) {
      throw new OrphanResourcesInOrganizationException(organization.getId());
    }
  }

  private boolean hasPermission(
      User user, Organization organization, OrganizationPermission permission) {
    return organization.getUsersInOrganization().stream()
        .anyMatch(
            userInOrganization ->
                userInOrganization.getUser().equals(user)
                    && userInOrganization.getOrganizationPermission().contains(permission));
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
