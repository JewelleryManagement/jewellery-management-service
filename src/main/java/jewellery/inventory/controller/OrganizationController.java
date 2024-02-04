package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.exception.not_found.NotFoundException;
import jewellery.inventory.model.Organization;
import jewellery.inventory.service.OrganizationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public List<OrganizationResponseDto> all() {
    List<Organization> organization = organizationService.all();
    return organization.stream()
      .map(OrganizationResponseDto::new)
      .collect(Collectors.toList());
  }

  @Operation(summary = "Get organization by id")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}")
  public OrganizationResponseDto show(@PathVariable UUID id) {
    Optional<Organization> organization = organizationService.show(id);
    if(organization.isEmpty()) {
      throw new NotFoundException("Organization not found");
    }
    return new OrganizationResponseDto(organization.get());
  }

  @Operation(summary = "Create a new operation")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public OrganizationResponseDto create(@RequestBody @Valid OrganizationRequestDto organizationRequestDto){
    return organizationService.create(organizationRequestDto);
  }

}
