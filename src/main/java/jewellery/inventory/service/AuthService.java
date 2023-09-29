package jewellery.inventory.service;

import java.util.Optional;
import jewellery.inventory.dto.request.AuthenticationRequestDto;
import jewellery.inventory.dto.response.AuthenticationResponseDto;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.exception.security.InvalidCredentialsException;
import jewellery.inventory.exception.security.jwt.JwtAuthenticationBaseException;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository repository;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenService jwtService;

  public AuthenticationResponseDto authenticate(AuthenticationRequestDto authRequest) {
    tryAuthenticate(authRequest);

    String userEmail = authRequest.getEmail();

    User user = getUserByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail));

    String token = generateTokenForUser(user);

    return new AuthenticationResponseDto(token);
  }

  private Optional<User> getUserByEmail(String email) {
    return repository.findByEmail(email);
  }

  private String generateTokenForUser(User user) {
    return jwtService.generateToken(user);
  }

  private void tryAuthenticate(AuthenticationRequestDto authRequest) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              authRequest.getEmail(), authRequest.getPassword()));
    } catch (JwtAuthenticationBaseException e) {
      throw new InvalidCredentialsException();
    }
  }
}
