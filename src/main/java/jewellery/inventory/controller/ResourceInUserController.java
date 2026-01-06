package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.response.PurchasedResourceQuantityResponseDto;
import jewellery.inventory.service.ResourceInUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources/availability")
@RequiredArgsConstructor
@Validated
public class ResourceInUserController {
  private final ResourceInUserService resourceAvailabilityService;

  @Operation(summary = "Get all purchased resources")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/purchased/{userId}")
  public List<PurchasedResourceQuantityResponseDto> getAllPurchasedResources(
      @PathVariable("userId") UUID userId) {
    return resourceAvailabilityService.getAllPurchasedResources(userId);
  }
}
