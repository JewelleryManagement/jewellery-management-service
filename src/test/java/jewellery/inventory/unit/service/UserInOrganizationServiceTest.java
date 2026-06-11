package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.ScopedRoleHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.*;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
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
  private Organization organization;
  private User user;
  private User newUser;
  private UserInOrganization userInOrganization;
  private UserInOrganizationResponseDto userInOrganizationResponseDto;
  private ScopedRoleRequestDto scopedRoleRequestDto;
  private ScopedRole scopedRole;
  private ScopedRoleResponseDto scopedRoleResponseDto;
  private RoleMembership roleMembership;
  private OrganizationResponseDto organizationResponseDto;
  private OrganizationSingleMemberResponseDto organizationSingleMemberResponseDto;
  private UserInOrganization newUserInOrganization;
  private UserInOrganizationRequestDto userInOrganizationRequestDto;

  @BeforeEach
  void setUp() {
    user = UserTestHelper.createSecondTestUser();
    newUser = UserTestHelper.createSecondTestUser();
    organization = getTestOrganizationWithUserWithAllPermissions(user);
    userInOrganization = getTestUserInOrganization(organization);
    userInOrganizationResponseDto = OrganizationTestHelper.getUserInOrganizationResponseDto(user);
    scopedRoleRequestDto = createOrganizationRoleRequest();
    scopedRole = createRole(scopedRoleRequestDto);
    scopedRoleResponseDto = createRoleResponse(scopedRole);
    roleMembership = new RoleMembership(UUID.randomUUID(), user, organization, scopedRole);
    organizationResponseDto = getTestOrganizationResponseDto(organization);
    organizationSingleMemberResponseDto =
        OrganizationTestHelper.createOrganizationSingleMemberResponseDto(
            userInOrganizationResponseDto, organizationResponseDto);
    newUserInOrganization = OrganizationTestHelper.createUserInOrganization(newUser, organization);
    userInOrganizationRequestDto =
        OrganizationTestHelper.getTestUserInOrganizationRequestWithRoles(
            newUser.getId(), List.of(scopedRole.getId()));
  }

  @Test
  void getUsersInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);

    when(organizationMapper.toUserInOrganizationResponseDto(
            organization.getUsersInOrganization().get(0)))
        .thenReturn(userInOrganizationResponseDto);

    List<UserInOrganizationResponseDto> actual =
        userInOrganizationService.getAllUsersInOrganization(organization.getId());

    assertNotNull(actual);
    assertEquals(
        actual.getFirst().getUser().getId(), userInOrganizationResponseDto.getUser().getId());
    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(organizationMapper, times(1))
        .toUserInOrganizationResponseDto(organization.getUsersInOrganization().get(0));
  }

  @Test
  void getAllUsersInOrganizationWithRolesSuccessfully() {
    userInOrganizationResponseDto.setOrganizationRoles(List.of(scopedRoleResponseDto));
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(roleMembershipRepository.findAllByOrganizationIdAndUserIds(
            organization.getId(), List.of(userInOrganization.getUser().getId())))
        .thenReturn(List.of(roleMembership));
    when(organizationMapper.toUserInOrganizationResponseDto(
            organization.getUsersInOrganization().get(0), List.of(scopedRole)))
        .thenReturn(userInOrganizationResponseDto);

    List<UserInOrganizationResponseDto> actual =
        userInOrganizationService.getAllUsersInOrganizationWithRoles(organization.getId());

    assertNotNull(actual);
    assertEquals(1, actual.size());
    assertFalse(actual.isEmpty());
    assertEquals(userInOrganizationResponseDto, actual.get(0));
    assertEquals(actual.getFirst().getOrganizationRoles().getFirst(), scopedRoleResponseDto);
    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(organizationService, times(1)).validateUserInOrganization(organization);
    verify(roleMembershipRepository, times(1))
        .findAllByOrganizationIdAndUserIds(
            organization.getId(), List.of(userInOrganization.getUser().getId()));
    verify(organizationMapper, times(1))
        .toUserInOrganizationResponseDto(
            organization.getUsersInOrganization().get(0), List.of(scopedRole));
  }

  @Test
  void updateUserRolesInOrganizationWhenUserHasNoRoleSuccessfully() {
    RoleMembership roleMembership = new RoleMembership();
    roleMembership.setRole(scopedRole);
    roleMembership.setUser(userInOrganization.getUser());
    roleMembership.setOrganization(organization);
    when(userInOrganizationRepository.findByUserIdAndOrganizationId(
            organization.getUsersInOrganization().get(0).getUser().getId(), organization.getId()))
        .thenReturn(Optional.of(userInOrganization));
    userInOrganizationResponseDto.setOrganizationRoles(List.of(scopedRoleResponseDto));
    when(roleMembershipRepository.findAllByOrganizationIdAndUserIds(
            organization.getId(),
            List.of(organization.getUsersInOrganization().get(0).getUser().getId())))
        .thenReturn(List.of(roleMembership));

    when(organizationMapper.toOrganizationSingleMemberResponseDto(
            userInOrganization, List.of(scopedRole)))
        .thenReturn(organizationSingleMemberResponseDto);

    OrganizationSingleMemberResponseDto actual =
        userInOrganizationService.updateUserRolesInOrganization(
            organization.getUsersInOrganization().get(0).getUser().getId(),
            organization.getId(),
            Set.of(scopedRole.getId()));

    assertNotNull(actual);
    verify(roleMembershipRepository, times(1))
        .deleteAllByUserAndOrganization(
            organization.getUsersInOrganization().get(0).getUser().getId(), organization.getId());
    verify(userInOrganizationRepository, times(1))
        .findByUserIdAndOrganizationId(
            organization.getUsersInOrganization().get(0).getUser().getId(), organization.getId());
    verify(organizationMapper, times(1))
        .toOrganizationSingleMemberResponseDto(userInOrganization, List.of(scopedRole));
  }

  @Test
  void updateUserRolesInOrganizationWhenUserHasRoleSuccessfully() {
    ScopedRoleRequestDto scopedRoleRequestDto = createOrganizationRoleRequest();
    ScopedRole scopedRole = createRole(scopedRoleRequestDto);
    ScopedRoleResponseDto scopedRoleResponseDto = createRoleResponse(scopedRole);
    userInOrganizationResponseDto.setOrganizationRoles(List.of(scopedRoleResponseDto));
    when(userInOrganizationRepository.findByUserIdAndOrganizationId(
            organization.getUsersInOrganization().get(0).getUser().getId(), organization.getId()))
        .thenReturn(Optional.of(userInOrganization));
    userInOrganizationResponseDto.setOrganizationRoles(Collections.emptyList());
    when(roleMembershipRepository.findAllByOrganizationIdAndUserIds(
            organization.getId(),
            List.of(organization.getUsersInOrganization().get(0).getUser().getId())))
        .thenReturn(Collections.emptyList());
    when(organizationMapper.toOrganizationSingleMemberResponseDto(
            userInOrganization, Collections.emptyList()))
        .thenReturn(organizationSingleMemberResponseDto);

    OrganizationSingleMemberResponseDto actual =
        userInOrganizationService.updateUserRolesInOrganization(
            organization.getUsersInOrganization().get(0).getUser().getId(),
            organization.getId(),
            Collections.emptySet());

    assertNotNull(actual);
    verify(roleMembershipRepository, times(1))
        .deleteAllByUserAndOrganization(
            organization.getUsersInOrganization().get(0).getUser().getId(), organization.getId());
    verify(roleMembershipRepository, times(1))
        .findAllByOrganizationIdAndUserIds(
            organization.getId(),
            List.of(organization.getUsersInOrganization().get(0).getUser().getId()));

    verify(organizationMapper, times(1))
        .toOrganizationSingleMemberResponseDto(userInOrganization, Collections.emptyList());
  }

  @Test
  void getUsersInOrganizationThrowsExceptionWhenUserIsNotPartOfOrganizationException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    doThrow(UserIsNotPartOfOrganizationException.class)
        .when(organizationService)
        .validateUserInOrganization(organization);

    assertThrows(
        UserIsNotPartOfOrganizationException.class,
        () -> userInOrganizationService.getAllUsersInOrganizationWithRoles(organization.getId()));
  }

  @Test
  void getUserInOrganizationSuccessfully() {
    when(userInOrganizationRepository.findByUserIdAndOrganizationId(
            userInOrganization.getId(), organization.getId()))
        .thenReturn(Optional.ofNullable(userInOrganization));
    when(organizationMapper.toUserInOrganizationResponseDto(userInOrganization))
        .thenReturn(userInOrganizationResponseDto);

    UserInOrganizationResponseDto response =
        userInOrganizationService.getUserInOrganization(
            organization.getId(), userInOrganization.getId());

    assertNotNull(response);
    assertEquals(response, userInOrganizationResponseDto);
    verify(userInOrganizationRepository, times(1))
        .findByUserIdAndOrganizationId(userInOrganization.getId(), organization.getId());
  }

  @Test
  void getUserInOrganizationThrowsExceptionWhenUserIsNotPartOfOrganization() {
    when(userInOrganizationRepository.findByUserIdAndOrganizationId(
            userInOrganization.getId(), organization.getId()))
        .thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () ->
            userInOrganizationService.getUserInOrganization(
                organization.getId(), userInOrganization.getId()));
    verify(userInOrganizationRepository, times(1))
        .findByUserIdAndOrganizationId(userInOrganization.getId(), organization.getId());
  }

  @Test
  void addUserInOrganizationThrowsExceptionOrganizationNotFoundException() {
    UUID uuid = UUID.randomUUID();
    when(organizationService.getOrganization(uuid)).thenThrow(OrganizationNotFoundException.class);
    assertThrows(
        OrganizationNotFoundException.class,
        () -> userInOrganizationService.addUserInOrganization(uuid, userInOrganization.getId()));

    verify(organizationService, times(1)).getOrganization(uuid);
  }

  @Test
  void addUserInOrganizationThrowsExceptionUserNotFoundException() {
    UUID uuid = UUID.randomUUID();
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(userService.getUser(uuid)).thenThrow(UserNotFoundException.class);
    assertThrows(
        UserNotFoundException.class,
        () -> userInOrganizationService.addUserInOrganization(organization.getId(), uuid));

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(userService, times(1)).getUser(uuid);
  }

  @Test
  void addUserInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(userService.getUser(newUser.getId())).thenReturn(newUser);
    when(userInOrganizationRepository.save(any(UserInOrganization.class)))
        .thenReturn(newUserInOrganization);
    when(organizationMapper.toOrganizationSingleMemberResponseDto(
            newUserInOrganization, Collections.emptyList()))
        .thenReturn(organizationSingleMemberResponseDto);

    OrganizationSingleMemberResponseDto responseDto =
        userInOrganizationService.addUserInOrganization(organization.getId(), newUser.getId());

    assertNotNull(responseDto);
    assertEquals(
        responseDto.getMember().getUser(),
        organizationSingleMemberResponseDto.getMember().getUser());
    assertEquals(
        responseDto.getOrganization(), organizationSingleMemberResponseDto.getOrganization());
    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(userService, times(2)).getUser(newUser.getId());
    verify(userInOrganizationRepository, times(1)).save(any(UserInOrganization.class));
    verify(organizationMapper, times(1))
        .toOrganizationSingleMemberResponseDto(newUserInOrganization, Collections.emptyList());
  }

  @Test
  void addUserInOrganizationWithRolesThrowsExceptionOrganizationNotFoundException() {
    UUID uuid = UUID.randomUUID();
    when(organizationService.getOrganization(uuid)).thenThrow(OrganizationNotFoundException.class);
    assertThrows(
        OrganizationNotFoundException.class,
        () ->
            userInOrganizationService.addUserInOrganizationWithRoles(
                uuid, userInOrganizationRequestDto));

    verify(organizationService, times(1)).getOrganization(uuid);
  }

  @Test
  void addUserInOrganizationWithRolesThrowsExceptionUserNotFoundException() {
    UUID uuid = UUID.randomUUID();
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(userService.getUser(uuid)).thenThrow(UserNotFoundException.class);
    userInOrganizationRequestDto.setUserId(uuid);

    assertThrows(
        UserNotFoundException.class,
        () ->
            userInOrganizationService.addUserInOrganizationWithRoles(
                organization.getId(), userInOrganizationRequestDto));

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(userService, times(1)).getUser(uuid);
  }

  @Test
  void addUserInOrganizationWithRolesSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(userService.getUser(newUser.getId())).thenReturn(newUser);
    when(userInOrganizationRepository.save(any(UserInOrganization.class)))
        .thenReturn(newUserInOrganization);
    organizationSingleMemberResponseDto
        .getMember()
        .setOrganizationRoles(List.of(scopedRoleResponseDto));
    when(organizationMapper.toOrganizationSingleMemberResponseDto(
            newUserInOrganization, Collections.emptyList()))
        .thenReturn(organizationSingleMemberResponseDto);

    OrganizationSingleMemberResponseDto responseDto =
        userInOrganizationService.addUserInOrganizationWithRoles(
            organization.getId(), userInOrganizationRequestDto);

    assertNotNull(responseDto);
    assertEquals(
        responseDto.getMember().getUser(),
        organizationSingleMemberResponseDto.getMember().getUser());
    assertEquals(
        responseDto.getOrganization(), organizationSingleMemberResponseDto.getOrganization());
    assertEquals(
        responseDto.getMember().getOrganizationRoles(),
        organizationSingleMemberResponseDto.getMember().getOrganizationRoles());
    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(userService, times(2)).getUser(newUser.getId());
    verify(userInOrganizationRepository, times(1)).save(any(UserInOrganization.class));
    verify(organizationMapper, times(1))
        .toOrganizationSingleMemberResponseDto(newUserInOrganization, Collections.emptyList());
  }
}
