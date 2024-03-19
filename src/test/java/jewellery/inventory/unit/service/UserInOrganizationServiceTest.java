package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.*;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.exception.organization.MissingOrganizationPermissionException;
import jewellery.inventory.exception.organization.UserIsNotPartOfOrganizationException;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.*;
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
class UserInOrganizationServiceTest {
  @InjectMocks private UserInOrganizationService userInOrganizationService;
  @Mock private OrganizationService organizationService;
  @Mock private AuthService authService;
  @Mock private UserService userService;
  @Mock private OrganizationMapper organizationMapper;
  @Mock private UserInOrganizationRepository userInOrganizationRepository;
  private Organization organization;
  private Organization organizationWithUserAllPermission;
  private User user;
  private UserInOrganization userInOrganization;
  private ExecutorResponseDto executorResponseDto;

  @BeforeEach
  void setUp() {
    organization = getTestOrganization();
    user = UserTestHelper.createSecondTestUser();
    organizationWithUserAllPermission = getTestOrganizationWithUserWithAllPermissions(user);
    executorResponseDto = getTestExecutor(user);
    userInOrganization = getTestUserInOrganization(organizationWithUserAllPermission);
  }

  @Test
  void getUsersInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organizationWithUserAllPermission.getId()))
        .thenReturn(organizationWithUserAllPermission);

    when(organizationMapper.toOrganizationMembersResponseDto(organizationWithUserAllPermission))
        .thenReturn(new OrganizationMembersResponseDto());

    OrganizationMembersResponseDto actual =
        userInOrganizationService.getAllUsersInOrganization(
            organizationWithUserAllPermission.getId());

    assertNotNull(actual);
  }

  @Test
  void updateUserPermissionsInOrganizationSuccessfully() {
    when(userInOrganizationRepository.findByUserIdAndOrganizationId(
            organizationWithUserAllPermission.getUsersInOrganization().get(0).getUser().getId(),
            organizationWithUserAllPermission.getId()))
        .thenReturn(Optional.of(userInOrganization));

    when(organizationMapper.toOrganizationSingleMemberResponseDto(userInOrganization))
        .thenReturn(new OrganizationSingleMemberResponseDto());

    OrganizationSingleMemberResponseDto actual =
        userInOrganizationService.updateUserPermissionsInOrganization(
            organizationWithUserAllPermission.getUsersInOrganization().get(0).getUser().getId(),
            organizationWithUserAllPermission.getId(),
            List.of(OrganizationPermission.DESTROY_ORGANIZATION));

    assertNotNull(actual);
  }

  @Test
  void deleteUserInOrganizationThrowsExceptionWhenNoManageUsersPermission() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);

    doThrow(MissingOrganizationPermissionException.class)
        .when(organizationService)
        .validateCurrentUserPermission(organization, OrganizationPermission.MANAGE_USERS);

    assertThrows(
        MissingOrganizationPermissionException.class,
        () ->
            userInOrganizationService.deleteUserInOrganization(user.getId(), organization.getId()));
  }

  @Test
  void getUsersInOrganizationThrowsExceptionWhenUserIsNotPartOfOrganizationException() {
    when(organizationService.getOrganization(organizationWithUserAllPermission.getId()))
        .thenReturn(organizationWithUserAllPermission);
    doThrow(UserIsNotPartOfOrganizationException.class)
        .when(organizationService)
        .validateUserInOrganization(organizationWithUserAllPermission);

    assertThrows(
        UserIsNotPartOfOrganizationException.class,
        () ->
            userInOrganizationService.getAllUsersInOrganization(
                organizationWithUserAllPermission.getId()));
  }
}
