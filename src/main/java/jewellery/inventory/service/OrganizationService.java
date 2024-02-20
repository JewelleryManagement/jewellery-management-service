package jewellery.inventory.service;

import java.util.*;

import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
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

  private List<Organization> getAll() {
    logger.debug("Fetching all organizations");
    return organizationRepository.findAll();
  }

  private Organization getOrganization(UUID id) {
    logger.info("Get organization by ID: {}", id);
    return organizationRepository
        .findById(id)
        .orElseThrow(() -> new OrganizationNotFoundException(id));
  }

  public List<OrganizationResponseDto> getAllOrganizationsResponses() {
    logger.debug("Fetching all organizationsResponses");
    return getAll().stream().map(organizationMapper::toResponse).toList();
  }

  public OrganizationResponseDto getOrganizationResponse(UUID id) {
    logger.info("Get organizationResponse by ID: {}", id);
    return organizationMapper.toResponse(getOrganization(id));
  }

  @LogCreateEvent(eventType = EventType.ORGANIZATION_CREATE)
  public OrganizationResponseDto create(OrganizationRequestDto organizationRequestDto) {
    Organization organization = organizationMapper.toEntity(organizationRequestDto);
    organization.setUserInOrganizations(List.of(getUserInOrganizationOwner(organization)));
    organization = organizationRepository.save(organization);
    logger.info("Organization created with ID: {}", organization.getId());
    return organizationMapper.toResponse(organization);
  }

  private UserInOrganization getUserInOrganizationOwner(Organization organization) {
    UserInOrganization userInOrganizationOwner = new UserInOrganization();
    User user = userService.getUser(authService.getCurrentUser().getId());
    userInOrganizationOwner.setUser(user);
    userInOrganizationOwner.setOrganization(organization);
    userInOrganizationOwner.setOrganizationPermission(
        Arrays.asList(OrganizationPermission.values()));
    logger.info("Created UserInOrganization for Organization ID: {}", organization.getId());
    return userInOrganizationOwner;
  }
}
