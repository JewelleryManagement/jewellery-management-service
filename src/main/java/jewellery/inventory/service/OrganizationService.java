package jewellery.inventory.service;

import java.util.*;

import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.UserInOrganizationResponseDto;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.organization.UserIsNotPartOfOrganizationException;
import jewellery.inventory.exception.organization.MissingOrganizationPermissionException;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.security.AuthService;
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
  private final UserInOrganizationRepository userInOrganizationRepository;

  public List<OrganizationResponseDto> getAllOrganizationsResponses() {
    logger.debug("Fetching all organizationsResponses");
    return getAll().stream().map(organizationMapper::toResponse).toList();
  }

  public OrganizationResponseDto getOrganizationResponse(UUID id) {
    logger.debug("Get organizationResponse by ID: {}", id);
    return organizationMapper.toResponse(getOrganization(id));
  }

  public List<UserInOrganizationResponseDto> getAllUsersInOrganization(UUID organizationId) {
    Organization organization = getOrganization(organizationId);
    validateUserInOrganization(organization);
    return organizationMapper.toUserInOrganizationResponseDtoResponse(organization);
  }

  @LogUpdateEvent(eventType = EventType.ORGANIZATION_USER_UPDATE)
  public UserInOrganizationResponseDto updateUserPermissionsInOrganization(
      UUID organizationId, UUID userId, List<OrganizationPermission> organizationPermissionList) {
    Organization organization = getOrganization(organizationId);
    User userForUpdate = userService.getUser(userId);

    validateUserPermission(organization, OrganizationPermission.MANAGE_USERS);
    changeUserPermissionInOrganization(organization, userForUpdate, organizationPermissionList);
    logger.info(
        "Successfully updated user permissions in the organization. Organization ID: {}, User ID: {}",
        organizationId,
        userId);
    return organizationMapper.toUserInOrganizationResponseDtoResponse(organization, userId);
  }

  @LogCreateEvent(eventType = EventType.ORGANIZATION_USER_CREATE)
  public UserInOrganizationResponseDto addUserInOrganization(
      UUID organizationId, UserInOrganizationRequestDto userInOrganizationRequestDto) {
    Organization organization = getOrganization(organizationId);
    validateUserPermission(organization, OrganizationPermission.MANAGE_USERS);

    UserInOrganization userInOrganization =
        createUserInOrganization(userInOrganizationRequestDto, organization);
    addUserToOrganization(userInOrganization, organization);

    return organizationMapper.toUserInOrganizationResponseDtoResponse(
        organization, userInOrganizationRequestDto.getUserId());
  }

  @LogDeleteEvent(eventType = EventType.ORGANIZATION_USER_DELETE)
  public void deleteUserInOrganization(UUID userId, UUID organizationId) {
    Organization organization = getOrganization(organizationId);
    User userForDelete = userService.getUser(userId);
    validateUserPermission(organization, OrganizationPermission.MANAGE_USERS);

    organization
        .getUsersInOrganization()
        .removeIf(userInOrg -> userInOrg.getUser().equals(userForDelete));

    organizationRepository.save(organization);
    logger.info(
        "Successfully deleted user in the organization. Organization ID: {}, User ID: {}",
        organizationId,
        userForDelete.getId());
  }

  @LogCreateEvent(eventType = EventType.ORGANIZATION_CREATE)
  public OrganizationResponseDto create(OrganizationRequestDto organizationRequestDto) {
    Organization organization = organizationMapper.toEntity(organizationRequestDto);
    makeCurrentUserOwner(organization);
    organization = organizationRepository.save(organization);
    logger.info("Organization created with ID: {}", organization.getId());
    return organizationMapper.toResponse(organization);
  }

  private void makeCurrentUserOwner(Organization organization) {
    UserInOrganization userInOrganizationOwner = new UserInOrganization();
    User user = userService.getUser(authService.getCurrentUser().getId());
    userInOrganizationOwner.setUser(user);
    userInOrganizationOwner.setOrganization(organization);
    userInOrganizationOwner.setOrganizationPermission(
        Arrays.asList(OrganizationPermission.values()));
    logger.info("Created UserInOrganization for Organization ID: {}", organization.getId());
    organization.setUsersInOrganization(List.of(userInOrganizationOwner));
  }

  private List<Organization> getAll() {
    return organizationRepository.findAll();
  }

  private Organization getOrganization(UUID id) {
    return organizationRepository
        .findById(id)
        .orElseThrow(() -> new OrganizationNotFoundException(id));
  }

  private void validateUserPermission(
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

  private void validateUserInOrganization(Organization organization) {
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

  private boolean hasPermission(
      User user, Organization organization, OrganizationPermission permission) {
    return organization.getUsersInOrganization().stream()
        .anyMatch(
            userInOrganization ->
                userInOrganization.getUser().equals(user)
                    && userInOrganization.getOrganizationPermission().contains(permission));
  }

  private UserInOrganization createUserInOrganization(
      UserInOrganizationRequestDto requestDto, Organization organization) {
    UserInOrganization userInOrganization = new UserInOrganization();
    userInOrganization.setUser(userService.getUser(requestDto.getUserId()));
    userInOrganization.setOrganization(organization);
    userInOrganization.setOrganizationPermission(requestDto.getOrganizationPermission());
    userInOrganizationRepository.save(userInOrganization);
    logger.debug(
        "Successfully created user in organization. User ID: {}, Organization ID: {}, Organization Permission: {}",
        userInOrganization.getUser().getId(),
        userInOrganization.getOrganization().getId(),
        userInOrganization.getOrganizationPermission());
    return userInOrganization;
  }

  private void addUserToOrganization(
      UserInOrganization userInOrganization, Organization organization) {
    organization.getUsersInOrganization().add(userInOrganization);
    logger.info(
        "Successfully added user in the organization. Organization ID: {}, User ID: {}",
        organization.getId(),
        userInOrganization.getUser().getId());
    organizationRepository.save(organization);
  }

  private void changeUserPermissionInOrganization(
      Organization organization,
      User userForUpdate,
      List<OrganizationPermission> organizationPermissionList) {

    organization.getUsersInOrganization().stream()
        .filter(userInOrg -> userInOrg.getUser().equals(userForUpdate))
        .findFirst()
        .ifPresent(
            userInOrg -> {
              userInOrg.setOrganizationPermission(organizationPermissionList);
              userInOrganizationRepository.save(userInOrg);
              logger.info(
                  "User permissions successfully changed in organization. User ID: {}, Organization ID: {}, New Permissions: {}",
                  userForUpdate.getId(),
                  organization.getId(),
                  organizationPermissionList);
            });
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
