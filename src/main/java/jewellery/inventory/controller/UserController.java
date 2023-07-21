package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.UserRequest;
import jewellery.inventory.dto.UserResponse;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
  @Autowired private UserService userService;

  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    List<UserResponse> userResponseList =
        UserMapper.INSTANCE.toUserResponseList(userService.getAllUsers());
    return ResponseEntity.ok(userResponseList);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
    UserResponse userResponse = UserMapper.INSTANCE.toUserResponse(userService.getUser(id));
    return ResponseEntity.ok(userResponse);
  }

  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest newUser) {
    UserResponse userResponse =
        UserMapper.INSTANCE.toUserResponse(
            userService.createUser(UserMapper.INSTANCE.toUserEntity(newUser)));
    return ResponseEntity.ok(userResponse);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable UUID id, @Valid @RequestBody UserRequest userRequest) {
    User userToUpdate = UserMapper.INSTANCE.toUserEntity(userRequest);
    userToUpdate.setId(id);
    UserResponse userResponse =
        UserMapper.INSTANCE.toUserResponse(userService.updateUser(userToUpdate));
    return ResponseEntity.ok(userResponse);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<HttpStatus> deleteUser(@PathVariable UUID id) {
    userService.deleteUser(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
