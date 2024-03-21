package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UpdateUserInOrganizationRequest;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationMembersResponseDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.OrganizationSingleMemberResponseDto;
import jewellery.inventory.service.OrganizationService;
import jewellery.inventory.service.UserInOrganizationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@AllArgsConstructor
public class OrganizationController {

  private final OrganizationService organizationService;
  private final UserInOrganizationService userInOrganizationService;

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
  public OrganizationSingleMemberResponseDto addUserInOrganization(
      @PathVariable UUID organizationId,
      @RequestBody @Valid UserInOrganizationRequestDto userInOrganizationRequestDto) {
    return userInOrganizationService.addUserInOrganization(
        organizationId, userInOrganizationRequestDto);
  }

  @Operation(summary = "Delete a user in organization")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("{organizationId}/users/{userId}")
  public void deleteUserInOrganization(
      @PathVariable UUID organizationId, @PathVariable UUID userId) {
    userInOrganizationService.deleteUserInOrganization(userId, organizationId);
  }

  @Operation(summary = "Delete an organization")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{organizationId}")
  public void deleteOrganization(@PathVariable UUID organizationId) {
    organizationService.delete(organizationId);
  }

  @Operation(summary = "Update a user permissions in organization")
  @ResponseStatus(HttpStatus.OK)
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
  @GetMapping("{organizationId}/users")
  public OrganizationMembersResponseDto getAllUsersInOrganization(
      @PathVariable UUID organizationId) {
    return userInOrganizationService.getAllUsersInOrganization(organizationId);
  }
}
