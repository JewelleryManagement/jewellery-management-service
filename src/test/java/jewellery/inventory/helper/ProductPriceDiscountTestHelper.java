package jewellery.inventory.helper;

import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;

import java.math.BigDecimal;

public class ProductPriceDiscountTestHelper {

  public static ProductPriceDiscount createTestProductPriceDiscount(Product product, Sale sale) {
    ProductPriceDiscount productPriceDiscount = new ProductPriceDiscount();
    productPriceDiscount.setDiscount(BigDecimal.ZERO);
    productPriceDiscount.setProduct(product);
    productPriceDiscount.setSale(sale);
    productPriceDiscount.setSalePrice(BigDecimal.ONE);
    return productPriceDiscount;
  }
}
