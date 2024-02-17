package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganization;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jewellery.inventory.dto.response.OrganizationResponseDto;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.not_found.ProductNotFoundException;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.repository.OrganizationRepository;
import jewellery.inventory.service.OrganizationService;
import jewellery.inventory.service.UserService;
import jewellery.inventory.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
  @InjectMocks private OrganizationService organizationService;
  @Mock private OrganizationRepository organizationRepository;
  @Mock private OrganizationMapper organizationMapper;
  @Mock private AuthService authService;
  @Mock private UserService userService;

  private  Organization organization;

  @BeforeEach
  void setUp() {
    organization = getTestOrganization();
  }

  @Test
  void testGetAllOrganizations() {

    List<Organization> organizations =
        Arrays.asList(organization, new Organization(), new Organization());

    when(organizationRepository.findAll()).thenReturn(organizations);

    List<OrganizationResponseDto> responses = organizationService.getAllOrganizationsResponses();

    assertEquals(organizations.size(), responses.size());
  }

  @Test
  void testGetOrganizationWhenItsFound() {

    when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));

    OrganizationResponseDto response = new OrganizationResponseDto();
    when(organizationMapper.toResponse(any())).thenReturn(response);

    OrganizationResponseDto actual =
            organizationService.getOrganizationResponse(organization.getId());

    assertEquals(response, actual);
    assertEquals(response.getId(), actual.getId());
    assertEquals(response.getName(), actual.getName());
    assertEquals(response.getAddress(), actual.getAddress());
    assertEquals(response.getNote(), actual.getNote());

  }

  @Test
  void testGetOrganizationShouldThrowWhenItsNotFound() {
    UUID fakeId = UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71");
    assertThrows(
            OrganizationNotFoundException.class,
            () -> organizationService.getOrganizationResponse(fakeId));
  }
}
