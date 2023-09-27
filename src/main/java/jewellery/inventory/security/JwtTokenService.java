package jewellery.inventory.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import jewellery.inventory.exception.security.jwt.JwtIsNotValidException;
import jewellery.inventory.exception.security.jwt.JwtMissingDateException;
import jewellery.inventory.exception.security.jwt.JwtNameInIsNotValidException;
import jewellery.inventory.exception.security.jwt.JwtExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

  @Value("${jwt.token.expiration}")
  private Long tokenExpiration;

  @Autowired
  private final JwtUtils jwtUtils;
  private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(Date.from(Instant.now()))
        .setExpiration(Date.from(Instant.now().plusMillis(tokenExpiration)))
        .signWith(jwtUtils.getSigningKey(), SIGNATURE_ALGORITHM)
        .compact();
  }

  public String generateToken(UserDetails userDetails) {
    if (userDetails == null) {
      throw new IllegalArgumentException("UserDetails cannot be null");
    }
    return generateToken(new HashMap<>(), userDetails);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    try {
      final String name = extractName(token);
      if (!name.equals(userDetails.getUsername())) {
        throw new JwtNameInIsNotValidException();
      }
      if (isTokenExpired(token)) {
        throw new JwtExpiredException();
      }
      return true;
    } catch (SignatureException e) {
      throw new JwtIsNotValidException();
    }
  }

  public String extractName(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  private boolean isTokenExpired(String token) {
    try {
      return extractExpiration(token).isBefore(Instant.now());
    } catch (AuthenticationException e) {
      throw new JwtExpiredException();
    }
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = jwtUtils.extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Instant extractExpiration(String token) {
    Date expirationDate = extractClaim(token, Claims::getExpiration);
    if (expirationDate == null) {
      throw new JwtMissingDateException();
    }
    return expirationDate.toInstant();
  }

}
