package jewellery.inventory.service;

import java.util.*;

import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.security.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final OrganizationMapper organizationMapper;
  private final AuthService authService;
  private final UserService userService;

  private List<Organization> getAll() {
    return organizationRepository.findAll();
  }

  private Organization getOrganization(UUID id) {
    return organizationRepository
        .findById(id)
        .orElseThrow(() -> new OrganizationNotFoundException(id));
  }

  public List<OrganizationResponseDto> getAllOrganizationsResponses() {
    return getAll().stream().map(organizationMapper::toResponse).toList();
  }

  public OrganizationResponseDto getOrganizationResponse(UUID id) {
    return organizationMapper.toResponse(getOrganization(id));
  }

  public OrganizationResponseDto create(OrganizationRequestDto organizationRequestDto) {
    Organization organization = organizationMapper.toEntity(organizationRequestDto);
    Organization createdOrganization = organizationRepository.save(organization);

    UserInOrganization userInOrganizationOwner = new UserInOrganization();
    User user = userService.getUser(authService.getCurrentUser().getId());
    userInOrganizationOwner.setUser(user);
    userInOrganizationOwner.setOrganization(organization);
    userInOrganizationOwner.setOrganizationPermission(
        Arrays.asList(OrganizationPermission.values()));

    organization.setUserInOrganizations(List.of(userInOrganizationOwner));

    return organizationMapper.toResponse(createdOrganization);
  }
}
