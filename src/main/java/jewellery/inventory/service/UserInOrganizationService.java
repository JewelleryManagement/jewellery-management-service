package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationMembersResponseDto;
import jewellery.inventory.dto.response.OrganizationSingleMemberResponseDto;
import jewellery.inventory.exception.organization.UserIsNotPartOfOrganizationException;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.repository.OrganizationRepository;
import jewellery.inventory.repository.UserInOrganizationRepository;
import jewellery.inventory.service.security.AuthService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserInOrganizationService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(UserInOrganizationService.class);
  private final OrganizationRepository organizationRepository;
  private final OrganizationMapper organizationMapper;
  private final AuthService authService;
  private final UserService userService;
  private final UserInOrganizationRepository userInOrganizationRepository;
  private final OrganizationService organizationService;

  public OrganizationMembersResponseDto getAllUsersInOrganization(UUID organizationId) {
    Organization organization = organizationService.getOrganization(organizationId);
    validateUserInOrganization(organization);
    return organizationMapper.toOrganizationMembersResponseDto(organization);
  }

  @LogUpdateEvent(eventType = EventType.ORGANIZATION_USER_UPDATE)
  public OrganizationSingleMemberResponseDto updateUserPermissionsInOrganization(
      UUID userId, UUID organizationId, List<OrganizationPermission> organizationPermissionList) {

    UserInOrganization userInOrganization =
        userInOrganizationRepository
            .findByUserIdAndOrganizationId(userId, organizationId)
            .orElseThrow(() -> new UserIsNotPartOfOrganizationException(userId, organizationId));

    Organization organization = userInOrganization.getOrganization();
    User userForUpdate = userInOrganization.getUser();

    organizationService.validateCurrentUserPermission(organization, OrganizationPermission.MANAGE_USERS);
    changeUserPermissionInOrganization(organization, userForUpdate, organizationPermissionList);
    logger.info(
        "Successfully updated user permissions in the organization. Organization ID: {}, User ID: {}",
        organizationId,
        userId);
    return organizationMapper.toOrganizationSingleMemberResponseDto(userId, organization);
  }

  @LogCreateEvent(eventType = EventType.ORGANIZATION_USER_CREATE)
  public OrganizationSingleMemberResponseDto addUserInOrganization(
      UUID organizationId, UserInOrganizationRequestDto userInOrganizationRequestDto) {
    Organization organization = organizationService.getOrganization(organizationId);
    organizationService.validateCurrentUserPermission(organization, OrganizationPermission.MANAGE_USERS);

    UserInOrganization userInOrganization =
        createUserInOrganization(userInOrganizationRequestDto, organization);
    addUserToOrganization(userInOrganization, organization);

    return organizationMapper.toOrganizationSingleMemberResponseDto(
        userInOrganizationRequestDto.getUserId(), organization);
  }

  @LogDeleteEvent(eventType = EventType.ORGANIZATION_USER_DELETE)
  public void deleteUserInOrganization(UUID userId, UUID organizationId) {
    Organization organization = organizationService.getOrganization(organizationId);
    organizationService.validateCurrentUserPermission(organization, OrganizationPermission.MANAGE_USERS);

    boolean isFoundAndDeleted =
        organization
            .getUsersInOrganization()
            .removeIf(userInOrg -> userInOrg.getUser().getId().equals(userId));

    if (!isFoundAndDeleted) {
      throw new UserIsNotPartOfOrganizationException(userId, organizationId);
    }

    organizationRepository.save(organization);
    logger.info(
        "Successfully deleted user in the organization. Organization ID: {}, User ID: {}",
        organizationId,
        userId);
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
    UserInOrganization userInOrganization =
        userInOrganizationRepository
            .findByUserIdAndOrganizationId((UUID) ids[0], (UUID) ids[1])
            .orElse(null);
    if (userInOrganization == null) {
      return null;
    }
    return organizationMapper.toOrganizationSingleMemberResponseDto(
        userInOrganization.getUser().getId(), userInOrganization.getOrganization());
  }
}
