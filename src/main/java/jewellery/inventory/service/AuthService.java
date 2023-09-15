package jewellery.inventory.service;

import jewellery.inventory.dto.auth.AuthenticationRequestDto;
import jewellery.inventory.dto.auth.AuthenticationResponseDto;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository repository;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthenticationResponseDto authenticate(AuthenticationRequestDto authRequest) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
    var user =
        repository.findByEmail(authRequest.getEmail()).orElseThrow(UserNotFoundException::new);

    String token = jwtService.generateToken(user);
    return new AuthenticationResponseDto(token);
  }
}
