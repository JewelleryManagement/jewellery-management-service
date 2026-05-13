package jewellery.inventory.service;

import java.util.*;
import java.util.stream.Collectors;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationSingleMemberResponseDto;
import jewellery.inventory.dto.response.UserInOrganizationResponseDto;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.exception.organization.UserIsPartOfOrganizationException;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.repository.RoleMembershipRepository;
import jewellery.inventory.repository.UserInOrganizationRepository;
import jewellery.inventory.service.security.AuthService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserInOrganizationService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(UserInOrganizationService.class);
  private final OrganizationMapper organizationMapper;
  private final UserService userService;
  private final UserInOrganizationRepository userInOrganizationRepository;
  private final OrganizationService organizationService;
  private final RoleMembershipRepository roleMembershipRepository;
  private final AuthService authService;

  public List<UserInOrganizationResponseDto> getAllUsersInOrganization(UUID organizationId) {
    Organization organization = organizationService.getOrganization(organizationId);
    organizationService.validateUserInOrganization(organization);

    return organization.getUsersInOrganization().stream()
        .map(organizationMapper::toUserInOrganizationResponseDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<UserInOrganizationResponseDto> getAllUsersInOrganizationWithRoles(
      UUID organizationId) {
    Organization organization = organizationService.getOrganization(organizationId);
    organizationService.validateUserInOrganization(organization);

    List<UserInOrganization> usersInOrganization = organization.getUsersInOrganization();

    if (usersInOrganization.isEmpty()) {
      return Collections.emptyList();
    }

    List<UUID> userIds =
        usersInOrganization.stream()
            .map(userInOrganization -> userInOrganization.getUser().getId())
            .toList();

    Map<UUID, List<ScopedRole>> rolesByUserId =
        roleMembershipRepository.findAllByOrganizationIdAndUserIds(organizationId, userIds).stream()
            .collect(
                Collectors.groupingBy(
                    membership -> membership.getUser().getId(),
                    Collectors.mapping(RoleMembership::getRole, Collectors.toList())));

    return usersInOrganization.stream()
        .map(
            userInOrganization ->
                organizationMapper.toUserInOrganizationResponseDto(
                    userInOrganization,
                    rolesByUserId.getOrDefault(
                        userInOrganization.getUser().getId(), Collections.emptyList())))
        .toList();
  }

  @Transactional
  @LogUpdateEvent(eventType = EventType.ORGANIZATION_USER_UPDATE)
  public OrganizationSingleMemberResponseDto updateUserRolesInOrganization(
      UUID userId, UUID organizationId, Set<UUID> organizationRolesIdList) {

    UserInOrganization userInOrganization =
        getUserInOrganizationByUserIdAndOrganizationId(userId, organizationId);

    roleMembershipRepository.deleteAllByUserAndOrganization(userId, organizationId);

    if (!organizationRolesIdList.isEmpty()) {
      roleMembershipRepository.insertAll(
          userId, organizationId, organizationRolesIdList.toArray(UUID[]::new));
    }

    List<ScopedRole> organizationRoles =
        roleMembershipRepository
            .findAllByOrganizationIdAndUserIds(organizationId, List.of(userId))
            .stream()
            .map(RoleMembership::getRole)
            .toList();

    return organizationMapper.toOrganizationSingleMemberResponseDto(
        userInOrganization, organizationRoles);
  }

  @Transactional
  @LogCreateEvent(eventType = EventType.ORGANIZATION_USER_CREATE)
  public OrganizationSingleMemberResponseDto addUserInOrganization(
      UUID organizationId, UserInOrganizationRequestDto userInOrganizationRequestDto) {

    Organization organization = organizationService.getOrganization(organizationId);

    validateUserIsNotPartOfOrganization(
        organization, userService.getUser(userInOrganizationRequestDto.getUserId()));

    UserInOrganization userInOrganization =
        createUserInOrganization(userInOrganizationRequestDto, organization);

    addUserToOrganization(userInOrganization, organization);

    if (!userInOrganizationRequestDto.getOrganizationRoles().isEmpty()) {
      roleMembershipRepository.insertAll(
          userInOrganization.getUser().getId(),
          organizationId,
          userInOrganizationRequestDto.getOrganizationRoles().toArray(UUID[]::new));
    }

    List<ScopedRole> organizationRoles =
        roleMembershipRepository
            .findAllByOrganizationIdAndUserIds(
                organizationId, List.of(userInOrganizationRequestDto.getUserId()))
            .stream()
            .map(RoleMembership::getRole)
            .toList();

    return organizationMapper.toOrganizationSingleMemberResponseDto(
        userInOrganization, organizationRoles);
  }

  @Transactional
  @LogDeleteEvent(eventType = EventType.ORGANIZATION_USER_DELETE)
  public void deleteUserInOrganization(UUID userId, UUID organizationId) {
    Organization organization = organizationService.getOrganization(organizationId);

    boolean isFoundAndDeleted =
        organization
            .getUsersInOrganization()
            .removeIf(userInOrg -> userInOrg.getUser().getId().equals(userId));

    if (!isFoundAndDeleted) {
      throw new UserNotFoundException(userId);
    }

    roleMembershipRepository.deleteAllByUserAndOrganization(userId, organizationId);

    organizationService.saveOrganization(organization);
    logger.info(
        "Successfully deleted user in the organization. Organization ID: {}, User ID: {}",
        organizationId,
        userId);
  }

  public UserInOrganizationResponseDto getUserInOrganization(UUID organizationId, UUID userId) {
    UserInOrganization userInOrganization =
        getUserInOrganizationByUserIdAndOrganizationId(userId, organizationId);

    return organizationMapper.toUserInOrganizationResponseDto(userInOrganization);
  }

  public Set<Permission> getCurrentUserPermissions(UUID organizationId) {
    UUID currentUserId = authService.getCurrentUser().getId();

    return roleMembershipRepository.findPermissionsByUserIdAndOrganizationId(
        currentUserId, organizationId);
  }

  private void validateUserIsNotPartOfOrganization(Organization organization, User userForAdd) {
    boolean isPart =
        organization.getUsersInOrganization().stream()
            .anyMatch(userInOrganization -> userInOrganization.getUser().equals(userForAdd));
    if (isPart) {
      throw new UserIsPartOfOrganizationException(userForAdd.getId(), organization.getId());
    }
  }

  private UserInOrganization createUserInOrganization(
      UserInOrganizationRequestDto requestDto, Organization organization) {
    UserInOrganization userInOrganization = new UserInOrganization();
    userInOrganization.setUser(userService.getUser(requestDto.getUserId()));
    userInOrganization.setOrganization(organization);
    userInOrganizationRepository.save(userInOrganization);
    logger.debug(
        "Successfully created user in organization. User ID: {}, Organization ID: {}",
        userInOrganization.getUser().getId(),
        userInOrganization.getOrganization().getId());
    return userInOrganization;
  }

  private void addUserToOrganization(
      UserInOrganization userInOrganization, Organization organization) {
    organization.getUsersInOrganization().add(userInOrganization);
    logger.info(
        "Successfully added user in the organization. Organization ID: {}, User ID: {}",
        organization.getId(),
        userInOrganization.getUser().getId());
    organizationService.saveOrganization(organization);
  }

  private UserInOrganization getUserInOrganizationByUserIdAndOrganizationId(
      UUID userId, UUID organizationId) {
    return userInOrganizationRepository
        .findByUserIdAndOrganizationId(userId, organizationId)
        .orElseThrow(() -> new UserNotFoundException(userId));
  }

  @Override
  public Object fetchEntity(Object... ids) {
    UUID userId = (UUID) ids[0];
    UUID organizationId = (UUID) ids[1];

    UserInOrganization userInOrganization =
        userInOrganizationRepository
            .findByUserIdAndOrganizationId(userId, organizationId)
            .orElse(null);

    if (userInOrganization == null) {
      return null;
    }

    List<ScopedRole> organizationRoles =
        roleMembershipRepository
            .findAllByOrganizationIdAndUserIds(organizationId, List.of(userId))
            .stream()
            .map(RoleMembership::getRole)
            .toList();

    return organizationMapper.toOrganizationSingleMemberResponseDto(
        userInOrganization, organizationRoles);
  }
}
