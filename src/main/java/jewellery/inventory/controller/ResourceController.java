package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resources")
@CrossOrigin(origins = "${cors.origins}")
public class ResourceController {

  private final ResourceService resourceService;

  public ResourceController(ResourceService resourceService) {
    this.resourceService = resourceService;
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public List<ResourceResponseDto> getAllResources() {
    return resourceService.getAllResources();
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}")
  public ResourceResponseDto getResourceById(@PathVariable("id") UUID id) {
    return resourceService.getResourceById(id);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ResourceResponseDto createResource(
      @RequestBody @Valid ResourceRequestDto resourceRequestDto) {

    return resourceService.createResource(resourceRequestDto);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public ResourceResponseDto updateResource(
      @PathVariable("id") UUID id, @Valid @RequestBody ResourceRequestDto resourceRequestDto) {
    return resourceService.updateResource(id, resourceRequestDto);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteResourceById(@PathVariable("id") UUID id) {
    resourceService.deleteResourceById(id);
  }
}
