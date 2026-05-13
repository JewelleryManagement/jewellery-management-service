package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.ScopedRoleHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.*;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.exception.organization.UserIsNotPartOfOrganizationException;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.repository.RoleMembershipRepository;
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
  @Mock private RoleMembershipRepository roleMembershipRepository;
  private Organization organizationWithUserAllPermission;
  private User user;
  private UserInOrganization userInOrganization;
  private UserInOrganizationResponseDto userInOrganizationResponseDto;
  private ScopedRoleRequestDto scopedRoleRequestDto;
  private ScopedRole scopedRole;
  private ScopedRoleResponseDto scopedRoleResponseDto;
  private RoleMembership roleMembership;
  private OrganizationResponseDto organizationResponseDto;
  private OrganizationSingleMemberResponseDto organizationSingleMemberResponseDto;

  @BeforeEach
  void setUp() {
    user = UserTestHelper.createSecondTestUser();
    organizationWithUserAllPermission = getTestOrganizationWithUserWithAllPermissions(user);
    userInOrganization = getTestUserInOrganization(organizationWithUserAllPermission);
    userInOrganizationResponseDto = OrganizationTestHelper.getUserInOrganizationResponseDto(user);
    scopedRoleRequestDto = createRoleRequest();
    scopedRole = createRole(scopedRoleRequestDto);
    scopedRoleResponseDto = createRoleResponse(scopedRole);
    roleMembership =
        new RoleMembership(UUID.randomUUID(), user, organizationWithUserAllPermission, scopedRole);
    organizationResponseDto = getTestOrganizationResponseDto(organizationWithUserAllPermission);
    organizationSingleMemberResponseDto = new OrganizationSingleMemberResponseDto();
    organizationSingleMemberResponseDto.setOrganization(organizationResponseDto);
    organizationSingleMemberResponseDto.setMember(userInOrganizationResponseDto);
  }

  @Test
  void getUsersInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organizationWithUserAllPermission.getId()))
        .thenReturn(organizationWithUserAllPermission);

    when(organizationMapper.toUserInOrganizationResponseDto(
            organizationWithUserAllPermission.getUsersInOrganization().get(0)))
        .thenReturn(userInOrganizationResponseDto);

    List<UserInOrganizationResponseDto> actual =
        userInOrganizationService.getAllUsersInOrganization(
            organizationWithUserAllPermission.getId());

    assertNotNull(actual);
    assertEquals(
        actual.getFirst().getUser().getId(), userInOrganizationResponseDto.getUser().getId());
    verify(organizationService, times(1))
        .getOrganization(organizationWithUserAllPermission.getId());
    verify(organizationMapper, times(1))
        .toUserInOrganizationResponseDto(
            organizationWithUserAllPermission.getUsersInOrganization().get(0));
  }

  @Test
  void getAllUsersInOrganizationWithRolesSuccessfully() {
    userInOrganizationResponseDto.setOrganizationRoles(List.of(scopedRoleResponseDto));
    when(organizationService.getOrganization(organizationWithUserAllPermission.getId()))
        .thenReturn(organizationWithUserAllPermission);
    when(roleMembershipRepository.findAllByOrganizationIdAndUserIds(
            organizationWithUserAllPermission.getId(),
            List.of(userInOrganization.getUser().getId())))
        .thenReturn(List.of(roleMembership));
    when(organizationMapper.toUserInOrganizationResponseDto(
            organizationWithUserAllPermission.getUsersInOrganization().get(0), List.of(scopedRole)))
        .thenReturn(userInOrganizationResponseDto);

    List<UserInOrganizationResponseDto> actual =
        userInOrganizationService.getAllUsersInOrganizationWithRoles(
            organizationWithUserAllPermission.getId());

    assertNotNull(actual);
    assertEquals(1, actual.size());
    assertFalse(actual.isEmpty());
    assertEquals(userInOrganizationResponseDto, actual.get(0));
    assertEquals(actual.getFirst().getOrganizationRoles().getFirst(), scopedRoleResponseDto);
    verify(organizationService, times(1))
        .getOrganization(organizationWithUserAllPermission.getId());
    verify(organizationService, times(1))
        .validateUserInOrganization(organizationWithUserAllPermission);
    verify(roleMembershipRepository, times(1))
        .findAllByOrganizationIdAndUserIds(
            organizationWithUserAllPermission.getId(),
            List.of(userInOrganization.getUser().getId()));
    verify(organizationMapper, times(1))
        .toUserInOrganizationResponseDto(
            organizationWithUserAllPermission.getUsersInOrganization().get(0), List.of(scopedRole));
  }

  @Test
  void updateUserRolesInOrganizationWhenUserHasNoRoleSuccessfully() {
    RoleMembership roleMembership = new RoleMembership();
    roleMembership.setRole(scopedRole);
    roleMembership.setUser(userInOrganization.getUser());
    roleMembership.setOrganization(organizationWithUserAllPermission);
    when(userInOrganizationRepository.findByUserIdAndOrganizationId(
            organizationWithUserAllPermission.getUsersInOrganization().get(0).getUser().getId(),
            organizationWithUserAllPermission.getId()))
        .thenReturn(Optional.of(userInOrganization));
    userInOrganizationResponseDto.setOrganizationRoles(List.of(scopedRoleResponseDto));
    when(roleMembershipRepository.findAllByOrganizationIdAndUserIds(
            organizationWithUserAllPermission.getId(),
            List.of(
                organizationWithUserAllPermission
                    .getUsersInOrganization()
                    .get(0)
                    .getUser()
                    .getId())))
        .thenReturn(List.of(roleMembership));

    when(organizationMapper.toOrganizationSingleMemberResponseDto(
            userInOrganization, List.of(scopedRole)))
        .thenReturn(organizationSingleMemberResponseDto);

    OrganizationSingleMemberResponseDto actual =
        userInOrganizationService.updateUserRolesInOrganization(
            organizationWithUserAllPermission.getUsersInOrganization().get(0).getUser().getId(),
            organizationWithUserAllPermission.getId(),
            Set.of(scopedRole.getId()));

    assertNotNull(actual);
    verify(roleMembershipRepository, times(1))
        .deleteAllByUserAndOrganization(
            organizationWithUserAllPermission.getUsersInOrganization().get(0).getUser().getId(),
            organizationWithUserAllPermission.getId());
    verify(userInOrganizationRepository, times(1))
        .findByUserIdAndOrganizationId(
            organizationWithUserAllPermission.getUsersInOrganization().get(0).getUser().getId(),
            organizationWithUserAllPermission.getId());
    verify(organizationMapper, times(1))
        .toOrganizationSingleMemberResponseDto(userInOrganization, List.of(scopedRole));
  }

  @Test
  void updateUserRolesInOrganizationWhenUserHasRoleSuccessfully() {
    ScopedRoleRequestDto scopedRoleRequestDto = createRoleRequest();
    ScopedRole scopedRole = createRole(scopedRoleRequestDto);
    ScopedRoleResponseDto scopedRoleResponseDto = createRoleResponse(scopedRole);
    userInOrganizationResponseDto.setOrganizationRoles(List.of(scopedRoleResponseDto));
    when(userInOrganizationRepository.findByUserIdAndOrganizationId(
            organizationWithUserAllPermission.getUsersInOrganization().get(0).getUser().getId(),
            organizationWithUserAllPermission.getId()))
        .thenReturn(Optional.of(userInOrganization));
    userInOrganizationResponseDto.setOrganizationRoles(Collections.emptyList());
    when(roleMembershipRepository.findAllByOrganizationIdAndUserIds(
            organizationWithUserAllPermission.getId(),
            List.of(
                organizationWithUserAllPermission
                    .getUsersInOrganization()
                    .get(0)
                    .getUser()
                    .getId())))
        .thenReturn(Collections.emptyList());
    when(organizationMapper.toOrganizationSingleMemberResponseDto(
            userInOrganization, Collections.emptyList()))
        .thenReturn(organizationSingleMemberResponseDto);

    OrganizationSingleMemberResponseDto actual =
        userInOrganizationService.updateUserRolesInOrganization(
            organizationWithUserAllPermission.getUsersInOrganization().get(0).getUser().getId(),
            organizationWithUserAllPermission.getId(),
            Collections.emptySet());

    assertNotNull(actual);
    verify(roleMembershipRepository, times(1))
        .deleteAllByUserAndOrganization(
            organizationWithUserAllPermission.getUsersInOrganization().get(0).getUser().getId(),
            organizationWithUserAllPermission.getId());
    verify(roleMembershipRepository, times(1))
        .findAllByOrganizationIdAndUserIds(
            organizationWithUserAllPermission.getId(),
            List.of(
                organizationWithUserAllPermission
                    .getUsersInOrganization()
                    .get(0)
                    .getUser()
                    .getId()));

    verify(organizationMapper, times(1))
        .toOrganizationSingleMemberResponseDto(userInOrganization, Collections.emptyList());
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
            userInOrganizationService.getAllUsersInOrganizationWithRoles(
                organizationWithUserAllPermission.getId()));
  }

  @Test
  void getUserInOrganizationSuccessfully() {
    when(userInOrganizationRepository.findByUserIdAndOrganizationId(
            userInOrganization.getId(), organizationWithUserAllPermission.getId()))
        .thenReturn(Optional.ofNullable(userInOrganization));
    when(organizationMapper.toUserInOrganizationResponseDto(userInOrganization))
        .thenReturn(userInOrganizationResponseDto);

    UserInOrganizationResponseDto response =
        userInOrganizationService.getUserInOrganization(
            organizationWithUserAllPermission.getId(), userInOrganization.getId());

    assertNotNull(response);
    assertEquals(response, userInOrganizationResponseDto);
    verify(userInOrganizationRepository, times(1))
        .findByUserIdAndOrganizationId(
            userInOrganization.getId(), organizationWithUserAllPermission.getId());
  }

  @Test
  void getUserInOrganizationThrowsExceptionWhenUserIsNotPartOfOrganization() {
    when(userInOrganizationRepository.findByUserIdAndOrganizationId(
            userInOrganization.getId(), organizationWithUserAllPermission.getId()))
        .thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () ->
            userInOrganizationService.getUserInOrganization(
                organizationWithUserAllPermission.getId(), userInOrganization.getId()));
    verify(userInOrganizationRepository, times(1))
        .findByUserIdAndOrganizationId(
            userInOrganization.getId(), organizationWithUserAllPermission.getId());
  }
}
