package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UpdateUserInOrganizationRequest;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.model.OrganizationPermission;
import jewellery.inventory.service.OrganizationService;
import jewellery.inventory.service.UserInOrganizationService;
import jewellery.inventory.utils.NotUsedYet;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@AllArgsConstructor
public class OrganizationController {

  private final OrganizationService organizationService;
  private final UserInOrganizationService userInOrganizationService;

  @Operation(summary = "Get all organizations for current user")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public List<OrganizationResponseDto> getAllOrganizationsForCurrentUser() {
    return organizationService.getAllOrganizationsResponsesForCurrentUser();
  }

  @Operation(summary = "Get organization by id")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@orgAuth.hasPermission(#id, 'ORGANIZATION_READ')")
  @GetMapping("/{id}")
  public OrganizationResponseDto getOrganizationById(@PathVariable UUID id) {
    return organizationService.getOrganizationResponse(id);
  }

  @Operation(summary = "Get organizations by permission")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/by-permission/{organizationPermission}")
  public List<OrganizationResponseDto> getOrganizationsByPermission(
      @PathVariable OrganizationPermission organizationPermission) {
    return organizationService.getOrganizationsByPermission(organizationPermission);
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
  @PreAuthorize("@orgAuth.hasPermission(#organizationId, 'ORGANIZATION_USER_ADD')")
  @PostMapping("/{organizationId}/users")
  public OrganizationSingleMemberResponseDto addUserInOrganization(
      @PathVariable UUID organizationId,
      @RequestBody @Valid UserInOrganizationRequestDto userInOrganizationRequestDto) {
    return userInOrganizationService.addUserInOrganization(
        organizationId, userInOrganizationRequestDto);
  }

  @Operation(summary = "Delete a user in organization")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("@orgAuth.hasPermission(#organizationId, 'ORGANIZATION_USER_DELETE')")
  @DeleteMapping("{organizationId}/users/{userId}")
  public void deleteUserInOrganization(
      @PathVariable UUID organizationId, @PathVariable UUID userId) {
    userInOrganizationService.deleteUserInOrganization(userId, organizationId);
  }

  @NotUsedYet(reason = "Pending frontend implementation")
  @Operation(summary = "Delete an organization")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("@orgAuth.hasPermission(#organizationId, 'ORGANIZATION_DELETE')")
  @DeleteMapping("/{organizationId}")
  public void deleteOrganization(@PathVariable UUID organizationId) {
    organizationService.delete(organizationId);
  }

  @Operation(summary = "Update a user permissions in organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@orgAuth.hasPermission(#organizationId, 'ORGANIZATION_PERMISSION_UPDATE')")
  @PutMapping("{organizationId}/users/{userId}")
  public OrganizationSingleMemberResponseDto updateUserPermissionsInOrganization(
      @PathVariable UUID organizationId,
      @PathVariable UUID userId,
      @RequestBody @Valid UpdateUserInOrganizationRequest updateUserInOrganizationRequest) {
    return userInOrganizationService.updateUserPermissionsInOrganization(
        userId, organizationId, updateUserInOrganizationRequest.getOrganizationPermission());
  }

  @Operation(summary = "Get all users in organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@orgAuth.hasPermission(#organizationId, 'ORGANIZATION_USER_READ')")
  @GetMapping("{organizationId}/users")
  public OrganizationMembersResponseDto getAllUsersInOrganization(
      @PathVariable UUID organizationId) {
    return userInOrganizationService.getAllUsersInOrganization(organizationId);
  }

  @Operation(summary = "Get user in organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@orgAuth.hasPermission(#organizationId, 'ORGANIZATION_USER_READ')")
  @GetMapping("{organizationId}/users/{userId}")
  public UserInOrganizationResponseDto getUserInOrganization(
      @PathVariable UUID organizationId, @PathVariable UUID userId) {
    return userInOrganizationService.getUserInOrganization(organizationId, userId);
  }

  @Operation(summary = "Get all products in organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@orgAuth.hasPermission(#organizationId, 'ORGANIZATION_PRODUCT_READ')")
  @GetMapping("/{organizationId}/products")
  public ProductsInOrganizationResponseDto getAllProductsInOrganization(
      @PathVariable UUID organizationId) {
    return organizationService.getProductsInOrganization(organizationId);
  }
  //
  //  @GetMapping("/organizations/{orgId}/resources")
  //  @PreAuthorize("@orgAuth.hasPermission(#orgId, 'ORGANIZATION_USER_ADD')")
  //  public ResponseEntity<String> getResources(@PathVariable UUID orgId) {
  //    return ResponseEntity.ok("resources");
  //  }
}
