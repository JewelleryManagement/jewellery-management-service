package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.service.OrganizationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

//  @Operation(summary = "Create a new operation")
//  @ResponseStatus(HttpStatus.CREATED)
//  @PostMapping
//  public OrganizationResponseDto create(@RequestBody @Valid OrganizationRequestDto organizationRequestDto){
//    return organizationService.create(organizationRequestDto);
//  }

}
