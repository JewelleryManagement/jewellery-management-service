package jewellery.inventory.unit.service;

import static jewellery.inventory.model.EventType.RESOURCE_CREATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.response.SystemEventLiteResponseDto;
import jewellery.inventory.dto.response.SystemEventResponseDto;
import jewellery.inventory.exception.not_found.NotFoundException;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.service.SystemEventService;
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

  private SystemEventLiteResponseDto systemEventLiteResponseDto1;
  private SystemEventLiteResponseDto systemEventLiteResponseDto2;
  private SystemEventResponseDto systemEventResponseDto;

  @BeforeEach
  void setUp() {
    systemEventLiteResponseDto1 = getSystemEventLiteResponseDto();
    systemEventLiteResponseDto2 = getSystemEventLiteResponseDto();
    systemEventResponseDto = getSystemEventResponseDto();
  }

  @Test
  void testGetAllEventsShouldReturnEmptyList() {
    List<SystemEventLiteResponseDto> events = systemEventService.getAllEvents();

    assertNotNull(events);
    assertEquals(0, events.size());
  }

  @Test
  void testGetAllEventsSuccessfully() {
    when(systemEventRepository.findAllWithoutRelatedIds())
        .thenReturn(List.of(systemEventLiteResponseDto1, systemEventLiteResponseDto2));

    List<SystemEventLiteResponseDto> events = systemEventService.getAllEvents();

    assertNotNull(events);
    assertEquals(2, events.size());
    assertEquals(events.getFirst(), systemEventLiteResponseDto1);

    verify(systemEventRepository, times(1)).findAllWithoutRelatedIds();
  }

  @Test
  void testGetSystemEventShouldThrow() {
    when(systemEventRepository.findByIdWithoutRelatedIds(systemEventResponseDto.getId()))
        .thenThrow(NotFoundException.class);

    assertThrows(
        NotFoundException.class,
        () -> systemEventService.getSystemEvent(systemEventResponseDto.getId()));
  }

  @Test
  void testGetSystemEventSuccessfully() {
    when(systemEventRepository.findByIdWithoutRelatedIds(systemEventResponseDto.getId()))
        .thenReturn(Optional.of(systemEventResponseDto));

    SystemEventResponseDto event =
        systemEventService.getSystemEvent(systemEventResponseDto.getId());

    assertNotNull(event);
    assertEquals(event, systemEventResponseDto);

    verify(systemEventRepository, times(1))
        .findByIdWithoutRelatedIds(systemEventResponseDto.getId());
  }

  @Test
  void testGetEventsRelatedToShouldReturnEmptyList() {
    List<SystemEventLiteResponseDto> events =
        systemEventService.getEventsRelatedTo(systemEventLiteResponseDto1.getId());

    assertNotNull(events);
    assertEquals(0, events.size());
  }

  @Test
  void testGetEventsRelatedToSuccessfully() {
    when(systemEventRepository.findByRelatedId(systemEventLiteResponseDto1.getId()))
        .thenReturn(List.of(systemEventLiteResponseDto1));

    List<SystemEventLiteResponseDto> events =
        systemEventService.getEventsRelatedTo(systemEventLiteResponseDto1.getId());

    assertNotNull(events);
    assertEquals(1, events.size());
    assertEquals(events.getFirst(), systemEventLiteResponseDto1);

    verify(systemEventRepository, times(1)).findByRelatedId(systemEventLiteResponseDto1.getId());
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
