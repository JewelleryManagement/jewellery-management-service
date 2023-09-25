package jewellery.inventory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jewellery.inventory.exception.jwt.InvalidJwtException;
import jewellery.inventory.exception.jwt.NoJwtTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";
  private static final int TOKEN_OFFSET = TOKEN_PREFIX.length();

  private final JwtTokenService jwtService;
  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if (isUnprotectedEndpoint(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      authenticateRequest(request);
    } catch (AuthenticationException e) {
      clearSecurityContextAndCommenceEntryPoint(request, response, e);
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean isUnprotectedEndpoint(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    return (requestURI.equals("/users") && request.getMethod().equals("POST"))
        || requestURI.startsWith("/auth/");
  }

  private void authenticateRequest(HttpServletRequest request) {
    String authHeader = request.getHeader(AUTHORIZATION_HEADER);
    validateJwtHeader(authHeader);

    String token = authHeader.substring(TOKEN_OFFSET);
    String userEmail = jwtService.extractName(token);
    validateUserEmail(userEmail);

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
      if (jwtService.isTokenValid(token, userDetails)) {
        setAuthenticationContext(request, userDetails);
      }
    }
  }

  private void validateJwtHeader(String authHeader) {
    if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
      throw new NoJwtTokenException();
    }
  }

  private void validateUserEmail(String userEmail) {
    if (userEmail == null || userEmail.trim().isEmpty()) {
      throw new InvalidJwtException();
    }
  }

  private void setAuthenticationContext(HttpServletRequest request, UserDetails userDetails) {
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }

  private void clearSecurityContextAndCommenceEntryPoint(
      HttpServletRequest request, HttpServletResponse response, Exception e) {
    SecurityContextHolder.clearContext();
    jwtAuthenticationEntryPoint.commence(request, response, e);
  }
}
