package jewellery.inventory.mapper;

import org.springframework.stereotype.Component;

@Component
public class StringTrimmer {
  public String trimString(String value) {
    return value == null ? null : value.trim();
  }
}
