package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganizationRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.organization.OrganizationProductsException;
import jewellery.inventory.exception.organization.OrganizationResourcesException;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInOrganization;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.OrganizationRepository;
import jewellery.inventory.repository.UserInOrganizationRepository;
import jewellery.inventory.service.OrganizationService;
import jewellery.inventory.service.UserInOrganizationService;
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
  @Mock private UserInOrganizationService userInOrganizationService;

  @Mock private OrganizationRepository organizationRepository;
  @Mock private OrganizationMapper organizationMapper;
  @Mock private AuthService authService;
  @Mock private UserService userService;
  @Mock private UserInOrganizationRepository userInOrganizationRepository;

  private Organization organization;
  private Organization organizationWithUserAllPermission;
  private User user;
  private OrganizationRequestDto organizationRequestDto;
  private OrganizationResponseDto organizationResponseDto;
  private ExecutorResponseDto executorResponseDto;

  @BeforeEach
  void setUp() {
    organization = getTestOrganization();
    organizationRequestDto = getTestOrganizationRequest();
    user = UserTestHelper.createSecondTestUser();
    organizationWithUserAllPermission = getTestOrganizationWithUserWithAllPermissions(user);
    executorResponseDto = getTestExecutor(user);
    organizationResponseDto = getTestOrganizationResponseDto(organization);
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

    when(organizationRepository.findById(organization.getId()))
        .thenReturn(Optional.of(organization));

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
  void createOrganizationSuccessfully() {
    when(organizationMapper.toEntity(organizationRequestDto)).thenReturn(organization);
    when(authService.getCurrentUser()).thenReturn(executorResponseDto);
    when(userService.getUser(user.getId())).thenReturn(user);
    when(organizationRepository.save(organization)).thenReturn(organization);
    when(organizationMapper.toResponse(organization)).thenReturn(organizationResponseDto);

    OrganizationResponseDto actual = organizationService.create(organizationRequestDto);
    assertNotNull(actual);
    assertEquals(actual, organizationResponseDto);
  }

  @Test
  void deleteOrganizationSuccessfully() {
    when(organizationRepository.findById(organizationWithUserAllPermission.getId()))
        .thenReturn(Optional.of(organizationWithUserAllPermission));
    when(authService.getCurrentUser()).thenReturn(executorResponseDto);
    when(userService.getUser(user.getId())).thenReturn(user);

    organizationService.delete(organizationWithUserAllPermission.getId());
    verify(organizationRepository, times(1)).delete(organizationWithUserAllPermission);
    verify(userService, times(1)).getUser(user.getId());
    verify(authService, times(1)).getCurrentUser();
  }

  @Test
  void deleteOrganizationThrowOrganizationNotFoundException() {
    when(organizationRepository.findById(organizationWithUserAllPermission.getId()))
        .thenThrow(OrganizationNotFoundException.class);

    assertThrows(
        OrganizationNotFoundException.class,
        () -> organizationService.delete(organizationWithUserAllPermission.getId()));
  }

  @Test
  void deleteOrganizationThrowOrganizationProductsException() {
    organizationWithUserAllPermission.setProductsOwned(List.of(new Product()));
    when(organizationRepository.findById(organizationWithUserAllPermission.getId()))
        .thenReturn(Optional.of(organizationWithUserAllPermission));

    when(authService.getCurrentUser()).thenReturn(executorResponseDto);
    when(userService.getUser(user.getId())).thenReturn(user);

    assertThrows(
        OrganizationProductsException.class,
        () -> organizationService.delete(organizationWithUserAllPermission.getId()));
  }

  @Test
  void deleteOrganizationThrowOrganizationResourcesException() {
    organizationWithUserAllPermission.setResourceInOrganization(
        List.of(new ResourceInOrganization()));
    when(organizationRepository.findById(organizationWithUserAllPermission.getId()))
        .thenReturn(Optional.of(organizationWithUserAllPermission));

    when(authService.getCurrentUser()).thenReturn(executorResponseDto);
    when(userService.getUser(user.getId())).thenReturn(user);

    assertThrows(
        OrganizationResourcesException.class,
        () -> organizationService.delete(organizationWithUserAllPermission.getId()));
  }

  @Test
  void testGetOrganizationShouldThrowWhenItsNotFound() {
    UUID fakeId = UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71");
    assertThrows(
        OrganizationNotFoundException.class,
        () -> organizationService.getOrganizationResponse(fakeId));
  }
}
