package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.service.SystemEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system-events")
@RequiredArgsConstructor
public class SystemEventController {

  private final SystemEventService eventService;

  @Operation(summary = "Get all system events")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public List<SystemEvent> getAllEvents() {
    return eventService.getAllEvents();
  }
}
