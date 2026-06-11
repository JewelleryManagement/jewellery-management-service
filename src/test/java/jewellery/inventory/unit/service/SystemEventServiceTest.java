package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static jewellery.inventory.helper.UserTestHelper.createTestUserResponseDto;
import static jewellery.inventory.model.EventType.RESOURCE_CREATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.*;
import jewellery.inventory.dto.response.SystemEventLiteResponseDto;
import jewellery.inventory.dto.response.SystemEventResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.forbidden.ForbiddenException;
import jewellery.inventory.exception.not_found.NoAuthenticatedUserException;
import jewellery.inventory.exception.not_found.NotFoundException;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.RoleType;
import jewellery.inventory.repository.RoleMembershipRepository;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.service.AuthorizationService;
import jewellery.inventory.service.SystemEventService;
import jewellery.inventory.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SystemEventServiceTest {
  @InjectMocks private SystemEventService systemEventService;

  @Mock private SystemEventRepository systemEventRepository;
  @Mock private AuthService authService;
  @Mock private RoleMembershipRepository roleMembershipRepository;
  @Mock private AuthorizationService authorizationService;

  private SystemEventLiteResponseDto systemEventLiteResponseDto1;
  private SystemEventResponseDto systemEventResponseDto;
  private UserResponseDto currentUser;

  @BeforeEach
  void setUp() {
    systemEventLiteResponseDto1 = getSystemEventLiteResponseDto();
    systemEventResponseDto = getSystemEventResponseDto();
    currentUser = createTestUserResponseDto(createTestUser());
  }

  @Test
  void testGetAllEventsShouldThrowWhenUserNotAuthenticated() {
    when(authService.getCurrentUser()).thenThrow(NoAuthenticatedUserException.class);

    assertThrows(NoAuthenticatedUserException.class, () -> systemEventService.getAllEvents());
  }

  @Test
  void testGetAllEventsShouldReturnEmptyArrayWhenUserHasSystemReadPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(true);

    List<SystemEventLiteResponseDto> allEvents = systemEventService.getAllEvents();
    assertNotNull(allEvents);
    assertEquals(0, allEvents.size());

    verify(authService, times(1)).getCurrentUser();
    verify(authorizationService, times(1)).hasSystemPermission(Permission.SYSTEM_EVENT_READ.name());
  }

  @Test
  void testGetAllEventsWhenUserHasSystemReadPermissionSuccessfully() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(true);
    when(systemEventRepository.findAllWithoutRelatedIds())
        .thenReturn(List.of(systemEventLiteResponseDto1));

    List<SystemEventLiteResponseDto> allEvents = systemEventService.getAllEvents();
    assertNotNull(allEvents);
    assertEquals(1, allEvents.size());
    assertEquals(allEvents.getFirst(), systemEventLiteResponseDto1);

    verify(authService, times(1)).getCurrentUser();
    verify(authorizationService, times(1)).hasSystemPermission(Permission.SYSTEM_EVENT_READ.name());
    verify(systemEventRepository, times(1)).findAllWithoutRelatedIds();
  }

  @Test
  void testGetAllEventsShouldReturnEmptyArrayWhenUserHasNoSystemReadPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(false);
    List<UUID> orgIds = new ArrayList<>();
    when(roleMembershipRepository.findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ))
        .thenReturn(orgIds);

    List<SystemEventLiteResponseDto> allEvents = systemEventService.getAllEvents();
    assertNotNull(allEvents);
    assertEquals(0, allEvents.size());

    verify(authService, times(1)).getCurrentUser();
    verify(authorizationService, times(1)).hasSystemPermission(Permission.SYSTEM_EVENT_READ.name());
    verify(roleMembershipRepository, times(1))
        .findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ);
  }

  @Test
  void testGetAllEventsWhenUserHasNoSystemReadPermissionSuccessfully() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(false);
    List<UUID> orgIds = new ArrayList<>();
    when(roleMembershipRepository.findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ))
        .thenReturn(orgIds);
    orgIds.add(UUID.randomUUID());
    when(systemEventRepository.findByRelatedOrganizationIds(orgIds))
        .thenReturn(List.of(systemEventLiteResponseDto1));

    List<SystemEventLiteResponseDto> allEvents = systemEventService.getAllEvents();
    assertNotNull(allEvents);
    assertEquals(1, allEvents.size());
    assertEquals(allEvents.getFirst(), systemEventLiteResponseDto1);

    verify(authService, times(1)).getCurrentUser();
    verify(authorizationService, times(1)).hasSystemPermission(Permission.SYSTEM_EVENT_READ.name());
    verify(roleMembershipRepository, times(1))
        .findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ);
    verify(systemEventRepository, times(1)).findByRelatedOrganizationIds(orgIds);
  }

  @Test
  void testGetEventsRelatedToShouldThrowWhenUserNotAuthenticated() {
    when(authService.getCurrentUser()).thenThrow(NoAuthenticatedUserException.class);

    assertThrows(
        NoAuthenticatedUserException.class,
        () -> systemEventService.getEventsRelatedTo(currentUser.getId()));
  }

  @Test
  void testGetEventsRelatedToShouldReturnEmptyArrayWhenUserHasSystemReadPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(true);

    List<SystemEventLiteResponseDto> allEvents =
        systemEventService.getEventsRelatedTo(currentUser.getId());
    assertNotNull(allEvents);
    assertEquals(0, allEvents.size());

    verify(authService, times(1)).getCurrentUser();
    verify(authorizationService, times(1)).hasSystemPermission(Permission.SYSTEM_EVENT_READ.name());
  }

  @Test
  void testGetEventsRelatedToWhenUserHasSystemReadPermissionSuccessfully() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(true);
    when(systemEventRepository.findByRelatedId(currentUser.getId()))
        .thenReturn(List.of(systemEventLiteResponseDto1));

    List<SystemEventLiteResponseDto> allEvents =
        systemEventService.getEventsRelatedTo(currentUser.getId());
    assertNotNull(allEvents);
    assertEquals(1, allEvents.size());
    assertEquals(allEvents.getFirst(), systemEventLiteResponseDto1);

    verify(authService, times(1)).getCurrentUser();
    verify(authorizationService, times(1)).hasSystemPermission(Permission.SYSTEM_EVENT_READ.name());
    verify(systemEventRepository, times(1)).findByRelatedId(currentUser.getId());
  }

  @Test
  void testGetEventsRelatedToShouldReturnEmptyArrayWhenUserHasNoSystemReadPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(false);
    List<UUID> orgIds = new ArrayList<>();
    when(roleMembershipRepository.findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ))
        .thenReturn(orgIds);

    List<SystemEventLiteResponseDto> allEvents =
        systemEventService.getEventsRelatedTo(currentUser.getId());
    assertNotNull(allEvents);
    assertEquals(0, allEvents.size());

    verify(authService, times(1)).getCurrentUser();
    verify(authorizationService, times(1)).hasSystemPermission(Permission.SYSTEM_EVENT_READ.name());
    verify(roleMembershipRepository, times(1))
        .findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ);
  }

  @Test
  void testGetEventsRelatedToWhenUserHasNoSystemReadPermissionSuccessfully() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(false);
    List<UUID> orgIds = new ArrayList<>();
    when(roleMembershipRepository.findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ))
        .thenReturn(orgIds);
    orgIds.add(UUID.randomUUID());
    when(systemEventRepository.findByRelatedIdAndRelatedOrganizationIds(
            currentUser.getId(), orgIds))
        .thenReturn(List.of(systemEventLiteResponseDto1));

    List<SystemEventLiteResponseDto> allEvents =
        systemEventService.getEventsRelatedTo(currentUser.getId());
    assertNotNull(allEvents);
    assertEquals(1, allEvents.size());
    assertEquals(allEvents.getFirst(), systemEventLiteResponseDto1);

    verify(authService, times(1)).getCurrentUser();
    verify(authorizationService, times(1)).hasSystemPermission(Permission.SYSTEM_EVENT_READ.name());
    verify(roleMembershipRepository, times(1))
        .findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ);
    verify(systemEventRepository, times(1))
        .findByRelatedIdAndRelatedOrganizationIds(currentUser.getId(), orgIds);
  }

  @Test
  void testGetSystemEventShouldThrowNotFoundException() {
    when(systemEventRepository.existsById(systemEventResponseDto.getId()))
        .thenThrow(NotFoundException.class);

    assertThrows(
        NotFoundException.class,
        () -> systemEventService.getSystemEvent(systemEventResponseDto.getId()));
  }

  @Test
  void testGetSystemEventWhenUserHasSystemReadPermissionSuccessfully() {
    when(systemEventRepository.existsById(systemEventResponseDto.getId())).thenReturn(true);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(true);
    when(systemEventRepository.findByIdWithoutRelatedIds(systemEventResponseDto.getId()))
        .thenReturn(Optional.ofNullable(systemEventResponseDto));

    SystemEventResponseDto systemEvent =
        systemEventService.getSystemEvent(systemEventResponseDto.getId());

    assertNotNull(systemEvent);
    assertEquals(systemEvent, systemEventResponseDto);

    verify(systemEventRepository, times(1)).existsById(systemEventResponseDto.getId());
    verify(authorizationService, times(1)).hasSystemPermission(Permission.SYSTEM_EVENT_READ.name());
    verify(systemEventRepository, times(1))
        .findByIdWithoutRelatedIds(systemEventResponseDto.getId());
  }

  @Test
  void testGetSystemEventShouldThrowWhenUserHasNoSystemReadOrOrganizationReadPermission() {
    when(systemEventRepository.existsById(systemEventResponseDto.getId())).thenReturn(true);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(false);
    when(authService.getCurrentUser()).thenReturn(currentUser);
    List<UUID> orgIds = new ArrayList<>();
    when(roleMembershipRepository.findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ))
        .thenReturn(orgIds);

    assertThrows(
        ForbiddenException.class,
        () -> systemEventService.getSystemEvent(systemEventResponseDto.getId()));
  }

  @Test
  void testGetSystemEventWhenUserHasOrganizationReadPermission() {
    when(systemEventRepository.existsById(systemEventResponseDto.getId())).thenReturn(true);
    when(authorizationService.hasSystemPermission(Permission.SYSTEM_EVENT_READ.name()))
        .thenReturn(false);
    when(authService.getCurrentUser()).thenReturn(currentUser);
    List<UUID> orgIds = new ArrayList<>();
    orgIds.add(UUID.randomUUID());
    when(roleMembershipRepository.findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ))
        .thenReturn(orgIds);
    when(systemEventRepository.findByIdAndRelatedOrganizationIds(
            systemEventResponseDto.getId(), orgIds))
        .thenReturn(Optional.ofNullable(systemEventResponseDto));

    SystemEventResponseDto systemEvent =
        systemEventService.getSystemEvent(systemEventResponseDto.getId());

    assertNotNull(systemEvent);
    assertEquals(systemEvent, systemEventResponseDto);

    verify(systemEventRepository, times(1)).existsById(systemEventResponseDto.getId());
    verify(authorizationService, times(1)).hasSystemPermission(Permission.SYSTEM_EVENT_READ.name());
    verify(authService, times(1)).getCurrentUser();
    verify(roleMembershipRepository, times(1))
        .findOrganizationIdsByUserIdAndPermission(
            currentUser.getId(), RoleType.ORGANIZATION, Permission.ORGANIZATION_EVENT_READ);
    verify(systemEventRepository, times(1))
        .findByIdAndRelatedOrganizationIds(systemEventResponseDto.getId(), orgIds);
  }

  private SystemEventLiteResponseDto getSystemEventLiteResponseDto() {
    return SystemEventLiteResponseDto.builder()
        .id(UUID.randomUUID())
        .type(RESOURCE_CREATE)
        .timestamp(Instant.now())
        .executor(new HashMap<>())
        .build();
  }

  private SystemEventResponseDto getSystemEventResponseDto() {
    return SystemEventResponseDto.builder()
        .id(UUID.randomUUID())
        .type(RESOURCE_CREATE)
        .timestamp(Instant.now())
        .executor(new HashMap<>())
        .payload(new HashMap<>())
        .build();
  }
}
