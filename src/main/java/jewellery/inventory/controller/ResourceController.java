package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import jewellery.inventory.dto.ResourceDTO;
import jewellery.inventory.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {

  @Autowired
  private final ResourceService resourceService;

  @GetMapping
  public ResponseEntity<List<ResourceDTO>> getAllResources() {
    return ResponseEntity.ok(resourceService.getAllResource());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ResourceDTO> getResourceById(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(resourceService.getResourceById(id));
  }

  @PostMapping
  public ResponseEntity<ResourceDTO> createResource(
      @RequestBody @Valid ResourceDTO resourceDTO, UriComponentsBuilder uriComponentsBuilder) {
    URI location =
        uriComponentsBuilder
            .path("/api/v1/resources/{id}")
            .buildAndExpand(resourceService.createResource(resourceDTO).getId())
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<ResourceDTO> updateResource(
      @PathVariable("id") UUID id, @Valid @RequestBody ResourceDTO resourceDTO) {
    return ResponseEntity.ok(resourceService.updateResource(id, resourceDTO));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteResourceById(@PathVariable("id") UUID id) {
    resourceService.deleteResourceById(id);
    return new ResponseEntity<>(
        "Resource with id: " + id + " has been deleted successfully!", HttpStatus.OK);
  }
}
