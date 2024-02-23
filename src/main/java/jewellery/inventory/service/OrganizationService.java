package jewellery.inventory.service;

import java.util.*;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.organization.UserNotHaveUserPermission;
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
public class OrganizationService {
  private static final Logger logger = LogManager.getLogger(OrganizationService.class);
  private final OrganizationRepository organizationRepository;
  private final OrganizationMapper organizationMapper;
  private final AuthService authService;
  private final UserService userService;

  public List<OrganizationResponseDto> getAllOrganizationsResponses() {
    logger.debug("Fetching all organizationsResponses");
    return getAll().stream().map(organizationMapper::toResponse).toList();
  }

  public OrganizationResponseDto getOrganizationResponse(UUID id) {
    logger.debug("Get organizationResponse by ID: {}", id);
    return organizationMapper.toResponse(getOrganization(id));
  }
  @LogCreateEvent(eventType = EventType.ORGANIZATION_USER_CREATE)
  public OrganizationResponseDto addUserInOrganization(
      UUID organizationId, UserInOrganizationRequestDto userInOrganizationRequestDto) {
    Organization organization = getOrganization(organizationId);
    User currentUser = userService.getUser(authService.getCurrentUser().getId());
    validateUserPermission(currentUser, organization);

    UserInOrganization userInOrganization =
        createUserInOrganization(userInOrganizationRequestDto, organization);
    addUserToOrganization(userInOrganization, organization);

    return organizationMapper.toResponse(organization);
  }

  private UserInOrganization createUserInOrganization(
      UserInOrganizationRequestDto requestDto, Organization organization) {
    UserInOrganization userInOrganization = new UserInOrganization();
    userInOrganization.setUser(userService.getUser(requestDto.getUserId()));
    userInOrganization.setOrganization(organization);
    userInOrganization.setOrganizationPermission(requestDto.getOrganizationPermission());
    return userInOrganization;
  }

  private void addUserToOrganization(
      UserInOrganization userInOrganization, Organization organization) {
    organization.getUsersInOrganization().add(userInOrganization);
    organizationRepository.save(organization);
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

  private void validateUserPermission(User user, Organization organization) {
    if (!hasManageUsersPermission(user, organization)) {
      throw new UserNotHaveUserPermission(user.getId(), organization.getId());
    }
  }

  private boolean hasManageUsersPermission(User user, Organization organization) {
    return organization.getUsersInOrganization().stream()
        .anyMatch(
            userInOrganization ->
                userInOrganization.getUser().equals(user)
                    && userInOrganization
                        .getOrganizationPermission()
                        .contains(OrganizationPermission.MANAGE_USERS));
  }
}
