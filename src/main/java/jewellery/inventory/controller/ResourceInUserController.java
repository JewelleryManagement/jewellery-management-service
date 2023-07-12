package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import jewellery.inventory.dto.ResourceInUserDTO;
import jewellery.inventory.services.ResourceInUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/resource-in-users")
@RequiredArgsConstructor
public class ResourceInUserController {
  private final ResourceInUserService resourceInUserService;

  @GetMapping
  public ResponseEntity<List<ResourceInUserDTO>> getAllResourceInUser() {
    return ResponseEntity.ok(resourceInUserService.getAllResourcesInUser());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ResourceInUserDTO> getResourceInUserById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(resourceInUserService.getResourceInUserById(id));
  }

  @PostMapping
  public ResponseEntity<ResourceInUserDTO> createResourceInUser(
      @RequestBody @Valid ResourceInUserDTO resourceInUserDTO,
      UriComponentsBuilder uriComponentsBuilder) {
    URI location =
        uriComponentsBuilder
            .path("/api/v1/resource-in-users/{id}")
            .buildAndExpand(resourceInUserService.createResourceInUser(resourceInUserDTO).getId())
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<ResourceInUserDTO> updateResourceInUser(
      @PathVariable("id") Long id, @Valid @RequestBody ResourceInUserDTO resourceInUserDTO) {
    return ResponseEntity.ok(resourceInUserService.updateResourceInUser(id, resourceInUserDTO));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteResourceInUserById(@PathVariable("id") Long id) {
    resourceInUserService.deleteResourceInUserById(id);
    return new ResponseEntity<>(
        "ResourceInUser with id: " + id + " has been deleted successfully!!", HttpStatus.OK);
  }
}
