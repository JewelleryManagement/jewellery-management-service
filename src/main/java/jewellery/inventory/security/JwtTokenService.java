package jewellery.inventory.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import jewellery.inventory.exception.security.InvalidSecretKeyException;
import jewellery.inventory.exception.security.jwt.InvalidNameInJwtException;
import jewellery.inventory.exception.security.jwt.InvalidJwtException;
import jewellery.inventory.exception.security.jwt.JwtExpiredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

  @Value("${jwt.secret.key}")
  private String secretKey;

  @Value("${jwt.token.expiration}")
  private Long tokenExpiration;

  private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(Date.from(Instant.now()))
        .setExpiration(Date.from(Instant.now().plusMillis(tokenExpiration)))
        .signWith(getSigningKey(), SIGNATURE_ALGORITHM)
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
        throw new InvalidNameInJwtException();
      }
      if (isTokenExpired(token)) {
        throw new JwtExpiredException();
      }
      return true;
    } catch (SignatureException e) {
      throw new InvalidJwtException();
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
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Instant extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration).toInstant();
  }

  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      throw new JwtExpiredException();
    } catch (SignatureException e) {
      throw new InvalidJwtException();
    }
  }

  private Key getSigningKey() {
    try {
      byte[] keyBytes = Decoders.BASE64.decode(secretKey);
      return Keys.hmacShaKeyFor(keyBytes);
    } catch (IllegalArgumentException e) {
      throw new InvalidSecretKeyException();
    }
  }
}
