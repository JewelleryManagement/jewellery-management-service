package jewellery.inventory.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import jewellery.inventory.exception.security.jwt.JwtExpiredException;
import jewellery.inventory.exception.security.jwt.JwtIsNotValidException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

  @Value("${jwt.token.expiration}")
  private Long tokenExpiration;

  @Autowired private final JwtUtils jwtUtils;
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
    isNameValid(token, userDetails);
    isTokenExpired(token);

    try {
      jwtUtils.extractAllClaims(token);
    } catch (JwtException e) {
      throw new JwtIsNotValidException();
    }
    return true;
  }

  private void isNameValid(String token, UserDetails userDetails) {
    final String name = extractName(token);
    if (!name.equals(userDetails.getUsername())) {
      throw new JwtIsNotValidException();
    }
  }

  public String extractName(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  private void isTokenExpired(String token) {
    if (extractExpiration(token).isBefore(Instant.now())) {
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
      throw new JwtExpiredException();
    }
    return expirationDate.toInstant();
  }
}
