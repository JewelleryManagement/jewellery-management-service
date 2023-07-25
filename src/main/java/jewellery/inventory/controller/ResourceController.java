package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

  @Autowired private final ResourceService resourceService;

  @GetMapping
  public ResponseEntity<List<ResourceResponseDto>> getAllResources() {
    return ResponseEntity.ok(resourceService.getAllResources());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ResourceResponseDto> getResourceById(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(resourceService.getResourceById(id));
  }

  @PostMapping
  public ResponseEntity<ResourceResponseDto> createResource(
      @RequestBody @Valid ResourceRequestDto resourceRequestDto) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(resourceService.createResource(resourceRequestDto));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ResourceResponseDto> updateResource(
      @PathVariable("id") UUID id, @Valid @RequestBody ResourceRequestDto resourceRequestDto) {
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(resourceService.updateResource(id, resourceRequestDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteResourceById(@PathVariable("id") UUID id) {
    resourceService.deleteResourceById(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body("Resource with id: " + id + " has been deleted successfully!");
  }
}
