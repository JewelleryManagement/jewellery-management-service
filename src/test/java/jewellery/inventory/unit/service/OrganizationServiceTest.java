package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganizationRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.ExecutorResponseDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.UserInOrganizationResponseDto;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.organization.MissingOrganizationPermissionException;
import jewellery.inventory.exception.organization.UserIsNotPartOfOrganizationException;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.OrganizationPermission;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.OrganizationRepository;
import jewellery.inventory.repository.UserInOrganizationRepository;
import jewellery.inventory.service.OrganizationService;
import jewellery.inventory.service.UserService;
import jewellery.inventory.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
  @InjectMocks private OrganizationService organizationService;
  @Mock private OrganizationRepository organizationRepository;
  @Mock private OrganizationMapper organizationMapper;
  @Mock private AuthService authService;
  @Mock private UserService userService;
  @Mock private UserInOrganizationRepository userInOrganizationRepository;

  private Organization organization;
  private Organization organizationWithUser;
  private User user;
  private OrganizationRequestDto organizationRequestDto;
  private OrganizationResponseDto organizationResponseDto;
  private UserInOrganizationResponseDto userInOrganizationResponseDto;
  private ExecutorResponseDto executorResponseDto;

  @BeforeEach
  void setUp() {
    organization = getTestOrganization();
    organizationRequestDto = getTestOrganizationRequest();
    user = UserTestHelper.createSecondTestUser();
    organizationWithUser = getTestOrganizationWithUser(user);
    executorResponseDto = getTestExecutor(user);
    organizationResponseDto = getTestOrganizationResponseDto(organization);
    userInOrganizationResponseDto = getTestUserInOrganizationResponseDto(organizationWithUser);
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
  void getUsersInOrganizationSuccessfully() {
    when(organizationRepository.findById(organizationWithUser.getId()))
        .thenReturn(Optional.of(organizationWithUser));
    when(authService.getCurrentUser()).thenReturn(executorResponseDto);
    when(userService.getUser(user.getId())).thenReturn(user);
    when(organizationMapper.toUserInOrganizationResponseDtoResponse(organizationWithUser))
        .thenReturn(new ArrayList<>());

    List<UserInOrganizationResponseDto> actual =
        organizationService.getAllUsersInOrganization(organizationWithUser.getId());

    assertNotNull(actual);
    assertEquals(0, actual.size());
  }

  @Test
  void updateUserPermissionsInOrganizationSuccessfully() {
    when(organizationRepository.findById(organizationWithUser.getId()))
        .thenReturn(Optional.of(organizationWithUser));
    when(authService.getCurrentUser()).thenReturn(executorResponseDto);
    when(userService.getUser(user.getId())).thenReturn(user);
    when(organizationMapper.toUserInOrganizationResponseDtoResponse(
            organizationWithUser, user.getId()))
        .thenReturn(userInOrganizationResponseDto);

    UserInOrganizationResponseDto actual =
        organizationService.updateUserPermissionsInOrganization(
            organizationWithUser.getId(),
            user.getId(),
            List.of(OrganizationPermission.DESTROY_ORGANIZATION));

    assertNotNull(actual);
    assertEquals(actual, userInOrganizationResponseDto);
  }

  @Test
  void testGetOrganizationShouldThrowWhenItsNotFound() {
    UUID fakeId = UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71");
    assertThrows(
        OrganizationNotFoundException.class,
        () -> organizationService.getOrganizationResponse(fakeId));
  }

  @Test
  void deleteUserInOrganizationThrowsExceptionWhenNoManageUsersPermission() {
    when(authService.getCurrentUser()).thenReturn(executorResponseDto);
    when(userService.getUser(executorResponseDto.getId())).thenReturn(new User());
    when(organizationRepository.findById(organizationWithUser.getId()))
        .thenReturn(Optional.of(organizationWithUser));

    assertThrows(
        MissingOrganizationPermissionException.class,
        () ->
            organizationService.deleteUserInOrganization(
                user.getId(), organizationWithUser.getId()));
  }

  @Test
  void updateUserPermissionsInOrganizationThrowsExceptionWhenNoManageUsersPermission() {
    when(organizationRepository.findById(organizationWithUser.getId()))
        .thenReturn(Optional.of(organizationWithUser));
    when(authService.getCurrentUser()).thenReturn(executorResponseDto);
    when(userService.getUser(executorResponseDto.getId())).thenReturn(new User());

    assertThrows(
        MissingOrganizationPermissionException.class,
        () ->
            organizationService.updateUserPermissionsInOrganization(
                organizationWithUser.getId(),
                user.getId(),
                List.of(OrganizationPermission.DESTROY_ORGANIZATION)));
  }

  @Test
  void getUsersInOrganizationThrowsExceptionWhenUserIsNotPartOfOrganizationException() {
    when(organizationRepository.findById(organizationWithUser.getId()))
        .thenReturn(Optional.of(organizationWithUser));
    when(authService.getCurrentUser()).thenReturn(executorResponseDto);
    when(userService.getUser(any(UUID.class))).thenReturn(new User());

    assertThrows(
        UserIsNotPartOfOrganizationException.class,
        () -> organizationService.getAllUsersInOrganization(organizationWithUser.getId()));
  }
}
