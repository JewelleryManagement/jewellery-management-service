package jewellery.inventory.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    Optional<User> userData = userService.getUser(id);

    return userData
        .map(user -> ResponseEntity.ok().body(user))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public User createUser(@Valid @RequestBody User user) {
    return userService.createUser(user);
  }

  @PutMapping("/{id}")
  public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
    Optional<User> userData = userService.getUser(id);

    if (userData.isPresent()) {
      User _user = userData.get();
      _user.setName(user.getName());
      _user.setEmail(user.getEmail());
      _user.setProductsOwned(user.getProductsOwned());
      _user.setResourcesOwned(user.getResourcesOwned());
      return ResponseEntity.ok().body(userService.updateUser(_user));
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<HttpStatus> deleteUser(@PathVariable UUID id) {
    try {
      userService.deleteUser(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
