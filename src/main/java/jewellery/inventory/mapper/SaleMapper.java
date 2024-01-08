package jewellery.inventory.mapper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.utils.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    saleResponseDto.setTotalPrice(getTotalPriceFromEntity(sale.getProducts()));
    saleResponseDto.setTotalDiscount(calculateDiscount(sale.getProducts(), PERCENTAGE));
    saleResponseDto.setTotalDiscountedPrice(calculateDiscount(sale.getProducts(), AMOUNT));
    saleResponseDto.setDate(sale.getDate());
    return saleResponseDto;
  }

  public Sale mapRequestToEntity(
      SaleRequestDto saleRequestDto, User seller, User buyer, List<Product> products) {
    Sale sale = new Sale();
    sale.setBuyer(buyer);
    sale.setSeller(seller);
    sale.setProducts(setProductPriceAndDiscount(saleRequestDto, products));
    sale.setDate(saleRequestDto.getDate());
    return sale;
  }

  private List<ProductResponseDto> mapAllProductsToResponse(Sale sale) {
    List<ProductResponseDto> productResponseDtos = new ArrayList<>();
    for (Product product : sale.getProducts()) {
      ProductResponseDto productResponseDto = productMapper.mapToProductResponseDto(product);
      productResponseDtos.add(productResponseDto);
    }
    return productResponseDtos;
  }

  private BigDecimal getTotalPriceFromEntity(List<Product> products) {
    BigDecimal totalPrice = new BigDecimal("0");
    for (Product product : products) {
      totalPrice = totalPrice.add(product.getSalePrice());
    }
    return totalPrice;
  }

  private BigDecimal calculateDiscount(List<Product> products, String calculationType) {
    BigDecimal totalDiscountAmount = new BigDecimal("0");
    BigDecimal totalPrice = new BigDecimal("0");

    for (Product product : products) {
      BigDecimal salePrice =
          Optional.ofNullable(product.getSalePrice()).orElse(new BigDecimal("0"));
      BigDecimal discountRate =
          Optional.ofNullable(product.getDiscount()).orElse(new BigDecimal("0"));
      BigDecimal discountAmount =
          salePrice.multiply(discountRate.divide(BigDecimalUtil.getBigDecimal("100"), RoundingMode.HALF_UP));
      totalDiscountAmount = totalDiscountAmount.add(discountAmount);
      totalPrice = totalPrice.add(salePrice);
    }

    if (PERCENTAGE.equals(calculationType) && !totalPrice.equals(BigDecimal.ZERO)) {
      return (totalDiscountAmount.divide(totalPrice, MathContext.DECIMAL128))
          .multiply(BigDecimalUtil.getBigDecimal("100"));
    } else if (AMOUNT.equals(calculationType)) {
      return totalPrice.subtract(totalDiscountAmount);
    }

    throw new IllegalArgumentException("Invalid calculation type");
  }

  private List<Product> setProductPriceAndDiscount(
      SaleRequestDto saleRequestDto, List<Product> products) {
    for (int i = 0; i < products.size(); i++) {
      if (saleRequestDto.getProducts().get(i).getSalePrice() != null) {
        products.get(i).setSalePrice(saleRequestDto.getProducts().get(i).getSalePrice());
        products.get(i).setDiscount(saleRequestDto.getProducts().get(i).getDiscount());
      }
    }
    return products;
  }
}
