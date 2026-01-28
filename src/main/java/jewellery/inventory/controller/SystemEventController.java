package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.response.SystemEventLiteResponseDto;
import jewellery.inventory.dto.response.SystemEventResponseDto;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.service.SystemEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system-events")
@RequiredArgsConstructor
public class SystemEventController {

  private final SystemEventService eventService;

  @Operation(summary = "Get all system events")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public List<SystemEventLiteResponseDto> getAllEvents() {
    return eventService.getAllEvents();
  }

  @Operation(summary = "Get all system events related to ID")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/related-to/{id}")
  public List<SystemEventLiteResponseDto> getEventsRelatedTo(@PathVariable("id") UUID id) {
    return eventService.getEventsRelatedTo(id);
  }

  @Operation(summary = "Get a singe system event")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}")
  public SystemEventResponseDto getSystemEvent(@PathVariable("id") UUID id) {
    return eventService.getSystemEvent(id);
  }
}
