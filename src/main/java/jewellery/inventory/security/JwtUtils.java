package jewellery.inventory.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import java.security.Key;
import jewellery.inventory.exception.security.InvalidSecretKeyException;
import jewellery.inventory.exception.security.jwt.JwtExpiredException;
import jewellery.inventory.exception.security.jwt.JwtIsNotValidException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

  @Value("${jwt.secret.key}")
  private String secretKey;

  public Key getSigningKey() {
    try {
      byte[] keyBytes = Decoders.BASE64.decode(secretKey);
      return Keys.hmacShaKeyFor(keyBytes);
    } catch (WeakKeyException e) {
      throw new InvalidSecretKeyException(e);
    }
  }

  public Claims extractAllClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      throw new JwtExpiredException();
    } catch (JwtException e) {
      throw new JwtIsNotValidException(e);
    }
  }
}
