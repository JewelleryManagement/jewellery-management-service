package jewellery.inventory.calculator;

import java.math.BigDecimal;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductPriceCalculator {
  private final ProductRepository productRepository;
  private final ResourceService resourceService;

  public BigDecimal calculateProductContentsPrice(Product product) {
    BigDecimal productsContentPrice =
        product.getProductsContent().stream()
            .map(
                pr -> {
                  BigDecimal salePrice =
                      productRepository.findById(pr.getId()).get().getSalePrice();
                  BigDecimal additionalPrice =
                      productRepository.findById(pr.getId()).get().getAdditionalPrice();
                  return salePrice.add(additionalPrice);
                })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal resourcesContentsPrice = calculateProductResourcesPrice(product);
    return productsContentPrice.add(resourcesContentsPrice);
  }

  private BigDecimal calculateProductResourcesPrice(Product product) {
    return product.getResourcesContent().stream()
        .map(
            resourceContent -> {
              BigDecimal resourceQuantity = resourceContent.getQuantity();
              ResourceResponseDto resourceResponseDto =
                  resourceService.getResource(resourceContent.getResource().getId());
              BigDecimal resourcePriceQuantity = resourceResponseDto.getPricePerQuantity();
              return resourceQuantity.multiply(resourcePriceQuantity);
            })
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
