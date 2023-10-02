package jewellery.inventory.controller;

import jakarta.validation.Valid;
import jewellery.inventory.dto.request.AuthenticationRequestDto;
import jewellery.inventory.dto.response.UserAuthDetailsDto;
import jewellery.inventory.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public UserAuthDetailsDto login(@Valid @RequestBody AuthenticationRequestDto authRequest) {
    return authService.login(authRequest);
  }
}
