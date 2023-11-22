package jewellery.inventory.unit.service.security;

import static org.junit.jupiter.api.Assertions.*;

import jewellery.inventory.exception.security.InvalidSecretKeyException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

  @InjectMocks private JwtUtils jwtUtils;

  private static final String SECRET_KEY = "3upwHfxELiEU8C7+DEOLJxQWVqKQ/abSpdzixM8YQ+I=";

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(jwtUtils, "secretKey", SECRET_KEY);
    SecurityContextHolder.clearContext();
  }

  @Test
  void willThrowWhenWeakKey() {
    String weakKey = "weakKey";
    ReflectionTestUtils.setField(jwtUtils, "secretKey", weakKey);

    assertThrows(InvalidSecretKeyException.class, () -> jwtUtils.getSigningKey());
  }
}
