package jewellery.inventory.mapper;

import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.PurchasedResourceInUserResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.PurchasedResourceInUser;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaleMapper {
  private final UserMapper userMapper;
  private final ProductMapper productMapper;
  private final PurchasedResourceInUserMapper purchasedResourceInUserMapper;
  private static final String AMOUNT = "amount";
  private static final String PERCENTAGE = "percentage";

  public SaleResponseDto mapEntityToResponseDto(Sale sale) {
    SaleResponseDto saleResponseDto = new SaleResponseDto();
    saleResponseDto.setId(sale.getId());
    saleResponseDto.setSeller(userMapper.toUserResponse(sale.getSeller()));
    saleResponseDto.setBuyer(userMapper.toUserResponse(sale.getBuyer()));
    saleResponseDto.setProducts(mapAllProductsToResponse(sale));
    saleResponseDto.setResources(mapAllResourcesToResponse(sale));
    saleResponseDto.setTotalPrice(getTotalPriceFromEntity(sale.getProducts()));
    saleResponseDto.setTotalDiscount(calculateDiscount(sale.getProducts(), PERCENTAGE));
    saleResponseDto.setTotalDiscountedPrice(calculateDiscount(sale.getProducts(), AMOUNT));
    saleResponseDto.setDate(sale.getDate());
    return saleResponseDto;
  }

  public Sale mapRequestToEntity(
      SaleRequestDto saleRequestDto,
      User seller,
      User buyer,
      List<Product> products,
      List<PurchasedResourceInUser> resources) {
    Sale sale = new Sale();
    sale.setBuyer(buyer);
    sale.setSeller(seller);
    sale.setProducts(setProductPriceAndDiscount(saleRequestDto, products));
    sale.setResources(setResourcePriceAndDiscount(saleRequestDto, resources));
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

  private List<ResourcesInUserResponseDto> mapAllResourcesToResponse(Sale sale) {
    return sale.getResources().stream()
        .map(purchasedResourceInUserMapper::toResourcesInUserResponseDto)
        .toList();
  }

  private BigDecimal getTotalPriceFromEntity(List<Product> products) {
    BigDecimal totalPrice = BigDecimal.ZERO;
    for (Product product : products) {
      totalPrice = totalPrice.add(product.getSalePrice());
    }
    return totalPrice;
  }

  private BigDecimal calculateDiscount(List<Product> products, String calculationType) {
    BigDecimal totalDiscountAmount = BigDecimal.ZERO;
    BigDecimal totalPrice = BigDecimal.ZERO;

    if (products.size() != 0) {
      for (Product product : products) {
        BigDecimal salePrice = Optional.ofNullable(product.getSalePrice()).orElse(BigDecimal.ZERO);
        BigDecimal discountRate =
            Optional.ofNullable(product.getDiscount()).orElse(BigDecimal.ZERO);
        BigDecimal discountAmount =
            salePrice.multiply(discountRate.divide(getBigDecimal("100"), RoundingMode.HALF_UP));
        totalDiscountAmount = totalDiscountAmount.add(discountAmount);
        totalPrice = totalPrice.add(salePrice);
      }

      if (PERCENTAGE.equals(calculationType) && !totalPrice.equals(BigDecimal.ZERO)) {
        return (totalDiscountAmount.divide(totalPrice, MathContext.DECIMAL128))
            .multiply(getBigDecimal("100"));
      } else if (AMOUNT.equals(calculationType)) {
        return totalPrice.subtract(totalDiscountAmount);
      }

      throw new IllegalArgumentException("Invalid calculation type");
    }
    return BigDecimal.ZERO;
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

  private List<PurchasedResourceInUser> setResourcePriceAndDiscount(
      SaleRequestDto saleRequestDto, List<PurchasedResourceInUser> resources) {
    for (int i = 0; i < resources.size(); i++) {
      if (saleRequestDto.getResources().get(i).getSalePrice() != null) {
        resources.get(i).setSalePrice(saleRequestDto.getResources().get(i).getSalePrice());
        resources.get(i).setDiscount(saleRequestDto.getResources().get(i).getDiscount());
      }
    }
    return resources;
  }
}
