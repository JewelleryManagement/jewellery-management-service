package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;

import jewellery.inventory.dto.request.ResourcePurchaseRequestDto;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.response.ResourceOwnedByUsersResponseDto;
import jewellery.inventory.dto.response.ResourcePurchaseResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.TransferResourceResponseDto;
import jewellery.inventory.service.ResourceInUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources/availability")
@RequiredArgsConstructor
public class ResourceInUserController {
  private final ResourceInUserService resourceAvailabilityService;

  @Operation(summary = "Transfer resource from user to another user")
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/transfer")
  public TransferResourceResponseDto transferResources(
      @RequestBody @Valid TransferResourceRequestDto transferResourceRequestDto) {
    return resourceAvailabilityService.transferResources(transferResourceRequestDto);
  }


  @Operation(summary = "Add resource to user")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ResourcesInUserResponseDto addResourceToUser(
      @RequestBody @Valid ResourcePurchaseRequestDto resourceUserDto) {
    return resourceAvailabilityService.addResourceToUser(resourceUserDto);
  }

  @Operation(summary = "Get resources by userId")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{userId}")
  public ResourcesInUserResponseDto getAllResourcesFromUser(@PathVariable UUID userId) {
    return resourceAvailabilityService.getAllResourcesFromUser(userId);
  }

  @Operation(summary = "Delete resources from user by userId and resourceId")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{userId}/{resourceId}")
  public void removeResourceFromUser(@PathVariable UUID userId, @PathVariable UUID resourceId) {
    resourceAvailabilityService.removeResourceFromUser(userId, resourceId);
  }

  @Operation(summary = "Delete specific amount of resource from user by userId, resourceId and quantity")
  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping("/{userId}/{resourceId}/{quantity}")
  public ResourcesInUserResponseDto removeQuantityFromUserResource(
      @PathVariable UUID userId, @PathVariable UUID resourceId, @PathVariable double quantity) {
    return resourceAvailabilityService.removeQuantityFromResource(userId, resourceId, quantity);
  }

  @Operation(summary = "Get all resources quantities by resourceId")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/by-resource/{resourceId}")
  public ResourceOwnedByUsersResponseDto getAllUsersAndQuantitiesByResource(
      @PathVariable UUID resourceId) {
    return resourceAvailabilityService.getUsersAndQuantities(resourceId);
  }
}
