package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import jewellery.inventory.dto.ResourceInProductDTO;
import jewellery.inventory.services.ResourceInProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/resources-in-products")
@RequiredArgsConstructor
public class ResourceInProductController {
  private final ResourceInProductService resourceInProductService;

  @GetMapping
  public ResponseEntity<List<ResourceInProductDTO>> getAllResourceInProduct() {
    return ResponseEntity.ok(resourceInProductService.getAllResourceInProduct());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ResourceInProductDTO> getResourceInProductById(
      @PathVariable("id") Long id) {
    return ResponseEntity.ok(resourceInProductService.getResourceInProductById(id));
  }

  @PostMapping
  public ResponseEntity<ResourceInProductDTO> createResourceInProduct(
      @RequestBody @Valid ResourceInProductDTO resourceInProductDTO,
      UriComponentsBuilder uriComponentsBuilder) {
    URI location =
        uriComponentsBuilder
            .path("/api/v1/resources-in-products/{id}")
            .buildAndExpand(
                resourceInProductService.createResourceInProduct(resourceInProductDTO).getId())
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<ResourceInProductDTO> updateResourceInProduct(
      @PathVariable("id") Long id, @Valid @RequestBody ResourceInProductDTO resourceInProductDTO) {
    return ResponseEntity.ok(
        resourceInProductService.updateResourceInProduct(id, resourceInProductDTO));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteResourceInProductById(@PathVariable("id") Long id) {
    resourceInProductService.deleteResourceInProductById(id);
    return new ResponseEntity<>(
        "ResourceInProduct with id: " + id + " has been deleted successfully!!", HttpStatus.OK);
  }
}
