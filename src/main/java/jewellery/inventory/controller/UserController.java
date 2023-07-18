package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.UserDto;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class UserController {
  @Autowired private UserService userService;

  @GetMapping
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUser(@PathVariable UUID id) {
    User user = userService.getUser(id);
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @PostMapping
  public User createUser(@Valid @RequestBody UserDto user) {
    return userService.createUser(UserMapper.INSTANCE.toEntity(user));
  }

  @PutMapping("/{id}")
  public ResponseEntity<User> updateUser(
      @PathVariable UUID id, @Valid @RequestBody UserDto userDto) {
    User user = UserMapper.INSTANCE.toEntity(userDto);
    user.setId(id);
    return new ResponseEntity<>(userService.updateUser(user), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<HttpStatus> deleteUser(@PathVariable UUID id) {
    userService.deleteUser(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
