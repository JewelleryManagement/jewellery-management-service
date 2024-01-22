package jewellery.inventory.mapper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;

@Component
@RequiredArgsConstructor
public class SaleMapper {
  private final UserMapper userMapper;
  private final ProductMapper productMapper;
  private static final String AMOUNT = "amount";
  private static final String PERCENTAGE = "percentage";

  public SaleResponseDto mapEntityToResponseDto(Sale sale) {
    SaleResponseDto saleResponseDto = new SaleResponseDto();
    saleResponseDto.setId(sale.getId());
    saleResponseDto.setSeller(userMapper.toUserResponse(sale.getSeller()));
    saleResponseDto.setBuyer(userMapper.toUserResponse(sale.getBuyer()));
    saleResponseDto.setProducts(mapAllProductsToResponse(sale));
    saleResponseDto.setTotalPrice(getTotalPriceFromEntities(sale.getProducts()));
    saleResponseDto.setTotalDiscount(calculateDiscount(sale.getProducts(), PERCENTAGE));
    saleResponseDto.setTotalDiscountedPrice(calculateDiscount(sale.getProducts(), AMOUNT));
    saleResponseDto.setDate(sale.getDate());
    return saleResponseDto;
  }

  public Sale mapRequestToEntity(
      SaleRequestDto saleRequestDto, User seller, User buyer, List<ProductPriceDiscount> products) {
    Sale sale = new Sale();
    sale.setBuyer(buyer);
    sale.setSeller(seller);
    sale.setProducts(products);
    sale.setDate(saleRequestDto.getDate());
    return sale;
  }

  private List<ProductResponseDto> mapAllProductsToResponse(Sale sale) {
    return sale.getProducts().stream()
        .map(productSale -> productMapper.mapToProductResponseDto(productSale.getProduct()))
        .toList();
  }

  public BigDecimal getTotalPriceFromEntities(List<ProductPriceDiscount> productResponseDtoList) {
    return productResponseDtoList.stream()
        .map(
            productPriceDiscount ->
                productMapper
                    .mapToProductResponseDto(productPriceDiscount.getProduct())
                    .getSalePrice())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calculateDiscount(
      List<ProductPriceDiscount> products, String calculationType) {
    BigDecimal totalDiscountAmount = BigDecimal.ZERO;
    BigDecimal totalPrice = BigDecimal.ZERO;

    for (ProductPriceDiscount product : products) {
      BigDecimal salePrice = productMapper
              .mapToProductResponseDto(product.getProduct()).getSalePrice();
      BigDecimal discountRate = product.getDiscount();
      BigDecimal discountAmount = salePrice.multiply(discountRate.divide(getBigDecimal("100")));
      totalDiscountAmount = totalDiscountAmount.add(discountAmount);
      totalPrice = totalPrice.add(salePrice);
    }

    if (PERCENTAGE.equals(calculationType)) {
     return totalDiscountAmount.divide(totalPrice).multiply(getBigDecimal("100"));
    } else if (AMOUNT.equals(calculationType)) {
      return totalPrice.subtract(totalDiscountAmount);
    }

    throw new IllegalArgumentException("Invalid calculation type");
  }
}
