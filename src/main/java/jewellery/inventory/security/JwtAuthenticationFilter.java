package jewellery.inventory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jewellery.inventory.exception.security.jwt.InvalidJwtException;
import jewellery.inventory.exception.security.jwt.JwtTokenNotFoundException;
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
    String token = validateJwtHeader(authHeader);
    String userEmail = extractUserEmail(token);

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
      if (jwtService.isTokenValid(token, userDetails)) {
        setAuthenticationContext(request, userDetails);
      }
    }
  }

  private String validateJwtHeader(String authHeader) {
    if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
      throw new JwtTokenNotFoundException();
    }
    return authHeader.substring(TOKEN_OFFSET);
  }

  private String extractUserEmail(String token) {
    String userEmail = jwtService.extractName(token);
    if (userEmail == null || userEmail.trim().isEmpty()) {
      throw new InvalidJwtException();
    }
    return userEmail;
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
