package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.service.ResourceService;
import jewellery.inventory.utils.NotUsedYet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {
  private final ResourceService resourceService;

  @Operation(summary = "Get all resources")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_RESOURCE_READ')")
  @GetMapping
  public List<ResourceResponseDto> getAllResources() {
    return resourceService.getAllResources();
  }

  @Operation(summary = "Get resource by resource id")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_RESOURCE_READ')")
  @GetMapping("/{id}")
  public ResourceResponseDto getResourceById(@PathVariable("id") UUID id) {
    return resourceService.getResource(id);
  }

  @Operation(summary = "Create new resource")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_RESOURCE_CREATE')")
  @PostMapping
  public ResourceResponseDto createResource(
      @RequestBody @Valid ResourceRequestDto resourceRequestDto) {
    return resourceService.createResource(resourceRequestDto);
  }

  @Operation(summary = "Update resource by resource id")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_RESOURCE_UPDATE')")
  @PutMapping("/{id}")
  public ResourceResponseDto updateResource(
      @PathVariable("id") UUID id, @Valid @RequestBody ResourceRequestDto resourceRequestDto) {
    return resourceService.updateResource(resourceRequestDto, id);
  }

  @Operation(summary = "Delete resource by resource id")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_RESOURCE_DELETE')")
  @DeleteMapping("/{id}")
  public void deleteResourceById(@PathVariable("id") UUID id) {
    resourceService.deleteResourceById(id);
  }

  @Operation(summary = "Get all resource quantities")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_RESOURCE_READ')")
  @GetMapping("/quantity")
  public List<ResourceQuantityResponseDto> getAllResourceQuantities() {
    return resourceService.getAllResourceQuantities();
  }

  @NotUsedYet(reason = "Pending frontend implementation")
  @Operation(summary = "Import resources from CSV")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_RESOURCE_IMPORT')")
  @PostMapping("/import")
  public List<ResourceResponseDto> importResources(@RequestParam("file") MultipartFile file) {
    return resourceService.importResources(file);
  }
}
