package jewellery.inventory.service.security;

import jewellery.inventory.dto.request.AuthenticationRequestDto;
import jewellery.inventory.dto.response.UserAuthDetailsDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.not_found.NoAuthenticatedUserException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.exception.security.InvalidCredentialsException;
import jewellery.inventory.exception.security.jwt.JwtAuthenticationBaseException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository repository;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenService jwtService;
  private final UserMapper userMapper;

  public UserAuthDetailsDto login(AuthenticationRequestDto authRequest) {
    User user = findUserByEmail(authRequest.getEmail());
    authenticate(authRequest);
    return createAuthUserResponse(user);
  }

  public UserResponseDto getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof User user) {
      return userMapper.toUserResponse(user);
    }
    throw new NoAuthenticatedUserException();
  }

  private void authenticate(AuthenticationRequestDto authRequest) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              authRequest.getEmail(), authRequest.getPassword()));
    } catch (JwtAuthenticationBaseException e) {
      throw new InvalidCredentialsException();
    }
  }

  private User findUserByEmail(String email) {
    return repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
  }

  private UserAuthDetailsDto createAuthUserResponse(User user) {
    UserResponseDto userResponse = userMapper.toUserResponse(user);
    String tokenResponse = generateTokenResponse(user);
    return new UserAuthDetailsDto(tokenResponse, userResponse);
  }

  private String generateTokenResponse(User user) {
    return jwtService.generateToken(user);
  }
}
