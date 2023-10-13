package jewellery.inventory.controller;

import java.util.List;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.service.SystemEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system-events")
@RequiredArgsConstructor
public class SystemEventController {

  private SystemEventService eventService;

  @GetMapping
  public ResponseEntity<List<SystemEvent>> getAllEvents() {
    return ResponseEntity.ok(eventService.getAllEvents());
  }
}
