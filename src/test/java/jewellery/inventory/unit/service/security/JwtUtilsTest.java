package jewellery.inventory.unit.service.security;

import static org.junit.jupiter.api.Assertions.*;

import jewellery.inventory.exception.security.InvalidSecretKeyException;
import jewellery.inventory.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

  @InjectMocks private JwtUtils jwtUtils;

  @Test
  void willThrowWhenWeakKey() {
    String weakKey = "weakKey";
    ReflectionTestUtils.setField(jwtUtils, "secretKey", weakKey);

    assertThrows(InvalidSecretKeyException.class, () -> jwtUtils.getSigningKey());
  }
}
