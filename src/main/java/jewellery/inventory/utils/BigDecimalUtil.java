package jewellery.inventory.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {
  public static BigDecimal getBigDecimal(String value) {
    return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
  }
}
