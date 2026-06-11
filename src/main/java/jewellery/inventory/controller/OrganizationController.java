package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UpdateUserInOrganizationRequest;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.model.Permission;
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
  @PreAuthorize("@auth.hasOrganizationPermission(#id, 'ORGANIZATION_READ')")
  @GetMapping("/{id}")
  public OrganizationResponseDto getOrganizationById(@PathVariable UUID id) {
    return organizationService.getOrganizationResponse(id);
  }

  @Operation(summary = "Get organizations by permission")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/by-permission/{permission}")
  public List<OrganizationResponseDto> getOrganizationsByPermission(
      @PathVariable Permission permission) {
    return organizationService.getOrganizationsByPermission(permission);
  }

  @Operation(summary = "Create a new organization")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("@auth.hasOrganizationPermission(#id, 'SYSTEM_ORGANIZATION_CREATE')")
  @PostMapping
  public OrganizationResponseDto create(
      @RequestBody @Valid OrganizationRequestDto organizationRequestDto) {
    return organizationService.create(organizationRequestDto);
  }

  @Operation(summary = "Add a user in organization")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("@auth.hasOrganizationPermission(#organizationId, 'ORGANIZATION_USER_ADD')")
  @PostMapping("/{organizationId}/users/{userId}")
  public OrganizationSingleMemberResponseDto addUserInOrganization(
      @PathVariable UUID organizationId, @PathVariable UUID userId) {
    return userInOrganizationService.addUserInOrganization(organizationId, userId);
  }

  @Operation(summary = "Add a user in organization with roles")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(
      "@auth.hasOrganizationPermission(#organizationId, 'ORGANIZATION_USER_ADD') && "
          + "@auth.hasOrganizationPermission(#organizationId, 'ORGANIZATION_ROLE_ASSIGN')")
  @PostMapping("/{organizationId}/users/roles")
  public OrganizationSingleMemberResponseDto addUserInOrganizationWithRoles(
      @PathVariable UUID organizationId, @RequestBody @Valid UserInOrganizationRequestDto request) {
    return userInOrganizationService.addUserInOrganizationWithRoles(organizationId, request);
  }

  @Operation(summary = "Delete a user in organization")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("@auth.hasOrganizationPermission(#organizationId, 'ORGANIZATION_USER_DELETE')")
  @DeleteMapping("{organizationId}/users/{userId}")
  public void deleteUserInOrganization(
      @PathVariable UUID organizationId, @PathVariable UUID userId) {
    userInOrganizationService.deleteUserInOrganization(userId, organizationId);
  }

  @NotUsedYet(reason = "Pending frontend implementation")
  @Operation(summary = "Delete an organization")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("@auth.hasOrganizationPermission(#organizationId, 'ORGANIZATION_DELETE')")
  @DeleteMapping("/{organizationId}")
  public void deleteOrganization(@PathVariable UUID organizationId) {
    organizationService.delete(organizationId);
  }

  @Operation(summary = "Update a user permissions in organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasOrganizationPermission(#organizationId, 'ORGANIZATION_ROLE_UPDATE')")
  @PutMapping("{organizationId}/users/{userId}")
  public OrganizationSingleMemberResponseDto updateUserRolesInOrganization(
      @PathVariable UUID organizationId,
      @PathVariable UUID userId,
      @RequestBody @Valid UpdateUserInOrganizationRequest updateUserInOrganizationRequest) {
    return userInOrganizationService.updateUserRolesInOrganization(
        userId, organizationId, updateUserInOrganizationRequest.getOrganizationRoles());
  }

  @Operation(summary = "Get all users in organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasOrganizationPermission(#organizationId, 'ORGANIZATION_USER_READ')")
  @GetMapping("{organizationId}/users")
  public List<UserInOrganizationResponseDto> getAllUsersInOrganization(
      @PathVariable UUID organizationId) {
    return userInOrganizationService.getAllUsersInOrganization(organizationId);
  }

  @Operation(summary = "Get all users in organization with roles")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasOrganizationPermission(#organizationId, 'ORGANIZATION_USER_READ')")
  @GetMapping("{organizationId}/users/roles")
  public List<UserInOrganizationResponseDto> getAllUsersInOrganizationWithRoles(
      @PathVariable UUID organizationId) {
    return userInOrganizationService.getAllUsersInOrganizationWithRoles(organizationId);
  }

  @Operation(summary = "Get user in organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasOrganizationPermission(#organizationId, 'ORGANIZATION_USER_READ')")
  @GetMapping("{organizationId}/users/{userId}")
  public UserInOrganizationResponseDto getUserInOrganization(
      @PathVariable UUID organizationId, @PathVariable UUID userId) {
    return userInOrganizationService.getUserInOrganization(organizationId, userId);
  }

  @Operation(summary = "Get all products in organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasOrganizationPermission(#organizationId, 'ORGANIZATION_PRODUCT_READ')")
  @GetMapping("/{organizationId}/products")
  public ProductsInOrganizationResponseDto getAllProductsInOrganization(
      @PathVariable UUID organizationId) {
    return organizationService.getProductsInOrganization(organizationId);
  }

  @Operation(summary = "Get current user permissions")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{organizationId}/permissions")
  public Set<Permission> getCurrentUserOrganizationPermissions(@PathVariable UUID organizationId) {
    return userInOrganizationService.getCurrentUserOrganizationPermissions(organizationId);
  }
}
