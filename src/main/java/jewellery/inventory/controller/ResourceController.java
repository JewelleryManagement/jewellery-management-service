package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.net.URI;
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
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

  @Autowired
  private final ResourceService resourceService;

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
          @RequestBody @Valid ResourceRequestDto resourceRequestDto, UriComponentsBuilder uriComponentsBuilder) {
    URI location =
        uriComponentsBuilder
            .path("/resources/{id}")
            .buildAndExpand(resourceService.createResource(resourceRequestDto).getId())
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<ResourceResponseDto> updateResource(
      @PathVariable("id") UUID id, @Valid @RequestBody ResourceRequestDto resourceRequestDto) {
    return ResponseEntity.ok(resourceService.updateResource(id, resourceRequestDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteResourceById(@PathVariable("id") UUID id) {
    resourceService.deleteResourceById(id);
    return new ResponseEntity<>(
        "Resource with id: " + id + " has been deleted successfully!", HttpStatus.OK);
  }
}
