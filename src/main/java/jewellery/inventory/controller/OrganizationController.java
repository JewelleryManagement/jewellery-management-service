package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UpdateUserPermissionsRequest;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.UserInOrganizationResponseDto;
import jewellery.inventory.service.OrganizationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@AllArgsConstructor
public class OrganizationController {

  private final OrganizationService organizationService;

  @Operation(summary = "Get all organizations")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public List<OrganizationResponseDto> getAllOrganizations() {
    return organizationService.getAllOrganizationsResponses();
  }

  @Operation(summary = "Get organization by id")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}")
  public OrganizationResponseDto getOrganizationById(@PathVariable UUID id) {
    return organizationService.getOrganizationResponse(id);
  }

  @Operation(summary = "Create a new organization")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public OrganizationResponseDto create(
      @RequestBody @Valid OrganizationRequestDto organizationRequestDto) {
    return organizationService.create(organizationRequestDto);
  }

  @Operation(summary = "Add a user in organization")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{organizationId}/users")
  public OrganizationResponseDto addUserInOrganization(
      @PathVariable UUID organizationId,
      @RequestBody @Valid UserInOrganizationRequestDto userInOrganizationRequestDto) {
    return organizationService.addUserInOrganization(organizationId, userInOrganizationRequestDto);
  }

  @Operation(summary = "Delete a user in organization")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{organizationId}/users/{userId}")
  public OrganizationResponseDto deleteUserInOrganization(
      @PathVariable UUID organizationId, @PathVariable UUID userId) {
    return organizationService.deleteUserInOrganization(userId, organizationId);
  }

  @Operation(summary = "Update a user permissions in organization")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("{organizationId}/users/{userId}")
  public OrganizationResponseDto updateUserPermissionsInOrganization(
      @PathVariable UUID organizationId,
      @PathVariable UUID userId,
      @RequestBody @Valid UpdateUserPermissionsRequest updateUserPermissionsRequest) {
    return organizationService.updateUserPermissionsInOrganization(
        organizationId, userId, updateUserPermissionsRequest.getOrganizationPermission());
  }

  @Operation(summary = "Get all users in organization")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("{organizationId}/users")
  public List<UserInOrganizationResponseDto> getAllUsersInOrganization(@PathVariable UUID organizationId) {
    return organizationService.getAllUsersInOrganization(organizationId);
  }
}
