package jewellery.inventory.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {
  public static BigDecimal getBigDecimal(String value) {
    return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
  }

  public static BigDecimal getBigDecimal(String value, int newScale) {
    return new BigDecimal(value).setScale(newScale, RoundingMode.HALF_UP);
  }

  public static BigDecimal getBigDecimal(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP);
  }
}
