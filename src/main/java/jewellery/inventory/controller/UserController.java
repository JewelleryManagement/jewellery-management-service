package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "${cors.origins}")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @Operation(summary = "Get all users")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public List<UserResponseDto> getAllUsers() {
    return userService.getAllUsers();
  }

  @Operation(summary = "Get user by id")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}")
  public UserResponseDto getUser(@PathVariable UUID id) {
    return userService.getUser(id);
  }

  @Operation(summary = "Create new user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User created successfully"),
      @ApiResponse(responseCode = "400", description = "Validation failed")
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public UserResponseDto createUser(@Valid @RequestBody UserRequestDto newUser) {
    return userService.createUser(newUser);
  }

  @Operation(summary = "Update user by id")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public UserResponseDto updateUser(
      @PathVariable UUID id, @Valid @RequestBody UserRequestDto userRequest) {
    return userService.updateUser(userRequest, id);
  }

  @Operation(summary = "Delete user by id")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteUser(@PathVariable UUID id) {
    userService.deleteUser(id);
  }
}
