package jewellery.inventory.calculator;

import java.math.BigDecimal;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.resource.ResourceInProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductPriceCalculator {

  public static BigDecimal calculateTotalPrice(Product product) {
    return calculateTotalPrice(product, BigDecimal.ZERO);
  }

  private static BigDecimal calculateTotalPrice(Product product, BigDecimal totalPrice) {
    if (product.getResourcesContent() != null) {
      for (ResourceInProduct resource : product.getResourcesContent()) {
        BigDecimal resourcePrice =
            resource.getQuantity().multiply(resource.getResource().getPricePerQuantity());
        totalPrice = totalPrice.add(resourcePrice);
      }
    }

    if (product.getAdditionalPrice() != null) {
      totalPrice = totalPrice.add(product.getAdditionalPrice());
    }

    if (product.getProductsContent() != null) {
      for (Product nestedProduct : product.getProductsContent()) {
        totalPrice = calculateTotalPrice(nestedProduct, totalPrice);
      }
    }

    return totalPrice;
  }
}
