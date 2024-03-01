package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.response.ResourceInOrganizationResponseDto;
import jewellery.inventory.service.ResourceInOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations/resources-availability")
@RequiredArgsConstructor
public class ResourceInOrganizationController {
    private final ResourceInOrganizationService resourceInOrganizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add resource to organization")
    public ResourceInOrganizationResponseDto addResourceToOrganization(@RequestBody @Valid ResourceInOrganizationRequestDto request) {
        return resourceInOrganizationService.addResourceToOrganization(request);
    }
}
