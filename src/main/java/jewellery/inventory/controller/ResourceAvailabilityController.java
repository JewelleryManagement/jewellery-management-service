package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceInUserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceInUserResponseDto;
import jewellery.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "${cors.origins}")
@RequiredArgsConstructor
public class ResourceAvailabilityController {
  private final UserService userService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public UserResponseDto addResourceToUser(
      @RequestBody @Valid ResourceInUserRequestDto resourceUserDto) {
    return userService.addResourceToUser(resourceUserDto);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{userId}/{resourceId}")
  public double getUserResourceQuantity(@PathVariable UUID userId, @PathVariable UUID resourceId) {
    return userService.getUserResourceQuantity(userId, resourceId);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{userId}")
  public List<ResourceInUserResponseDto> getAllResourcesFromUser(@PathVariable UUID userId) {
    return userService.getAllResourcesFromUser(userId);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{userId}/{resourceId}")
  public void removeResourceFromUser(@PathVariable UUID userId, @PathVariable UUID resourceId) {
    userService.removeResourceFromUser(userId, resourceId);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{userId}/{resourceId}/{quantity}")
  public void removeQuantityFromUserResource(
      @PathVariable UUID userId, @PathVariable UUID resourceId, @PathVariable int quantity) {
    userService.removeQuantityFromResource(userId, resourceId, quantity);
  }
}
