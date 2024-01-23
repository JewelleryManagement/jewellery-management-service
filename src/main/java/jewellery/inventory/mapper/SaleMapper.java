package jewellery.inventory.mapper;

import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.PurchasedResourceInUserResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.PurchasedResourceInUser;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaleMapper {
  private final UserMapper userMapper;
  private final ProductMapper productMapper;
  private final PurchasedResourceInUserMapper purchasedResourceInUserMapper;
  private final ResourceService resourceService;
  private static final String AMOUNT = "amount";
  private static final String PERCENTAGE = "percentage";

  public SaleResponseDto mapEntityToResponseDto(Sale sale) {
    SaleResponseDto saleResponseDto = new SaleResponseDto();
    saleResponseDto.setId(sale.getId());
    saleResponseDto.setSeller(userMapper.toUserResponse(sale.getSeller()));
    saleResponseDto.setBuyer(userMapper.toUserResponse(sale.getBuyer()));
    saleResponseDto.setProducts(mapAllProductsToResponse(sale));
    saleResponseDto.setResources(mapAllResourcesToResponse(sale));
    saleResponseDto.setTotalPrice(getTotalPriceFromEntity(sale.getProducts(), sale.getResources()));
    saleResponseDto.setTotalDiscount(
        calculateDiscount(sale.getProducts(), sale.getResources(), PERCENTAGE));
    saleResponseDto.setTotalDiscountedPrice(
        calculateDiscount(sale.getProducts(), sale.getResources(), AMOUNT));
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
    sale.setResources(setResourcesFields(saleRequestDto, resources));
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

  private List<PurchasedResourceInUserResponseDto> mapAllResourcesToResponse(Sale sale) {
    return sale.getResources().stream()
        .map(purchasedResourceInUserMapper::toPurchasedResourceInUserResponseDto)
        .collect(Collectors.toList());
  }

  private BigDecimal getTotalPriceFromEntity(
      List<Product> products, List<PurchasedResourceInUser> resources) {
    BigDecimal totalPrice = BigDecimal.ZERO;
    for (Product product : products) {
      totalPrice = totalPrice.add(product.getSalePrice());
    }

    for (PurchasedResourceInUser resource : resources) {
      totalPrice = totalPrice.add(resource.getResource().getPricePerQuantity());
    }

    return totalPrice;
  }

  private BigDecimal calculateDiscount(
      List<Product> products, List<PurchasedResourceInUser> resources, String calculationType) {
    BigDecimal totalDiscountAmount = BigDecimal.ZERO;
    BigDecimal totalPrice = BigDecimal.ZERO;

    if (!products.isEmpty()) {
      for (Product product : products) {
        BigDecimal salePrice = Optional.ofNullable(product.getSalePrice()).orElse(BigDecimal.ZERO);
        BigDecimal discountRate =
            Optional.ofNullable(product.getDiscount()).orElse(BigDecimal.ZERO);
        BigDecimal discountAmount =
                salePrice.multiply(discountRate).divide(getBigDecimal("100"), RoundingMode.HALF_UP);
        totalDiscountAmount = totalDiscountAmount.add(discountAmount);
        totalPrice = totalPrice.add(salePrice);
      }
    }

    if (!resources.isEmpty()) {
      for (PurchasedResourceInUser resource : resources) {
        BigDecimal salePrice =
            Optional.ofNullable(resource.getResource().getPricePerQuantity())
                .orElse(BigDecimal.ZERO);
        BigDecimal discountRate =
            Optional.ofNullable(resource.getDiscount()).orElse(BigDecimal.ZERO);

        BigDecimal discountAmount =
                salePrice.multiply(discountRate).divide(getBigDecimal("100"), RoundingMode.HALF_UP);
        totalDiscountAmount = totalDiscountAmount.add(discountAmount);
        totalPrice = totalPrice.add(salePrice);
      }
    }

    getTotalDiscountAmount(calculationType, totalPrice, totalDiscountAmount);

    return BigDecimal.ZERO;
  }

  private static BigDecimal getTotalDiscountAmount(
      String calculationType, BigDecimal totalPrice, BigDecimal totalDiscountAmount) {
    if (PERCENTAGE.equals(calculationType) && !totalPrice.equals(BigDecimal.ZERO)) {
      return (totalDiscountAmount.divide(totalPrice, MathContext.DECIMAL128))
          .multiply(getBigDecimal("100"));
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

  private List<PurchasedResourceInUser> setResourcesFields(
      SaleRequestDto saleRequestDto, List<PurchasedResourceInUser> resources) {
    for (int i = 0; i < resources.size(); i++) {
      if (saleRequestDto.getResources() != null) {
        Resource resource =
            resourceService.getResourceById(
                saleRequestDto.getResources().get(i).getResource().getResourceId());
        resources.get(i).setResource(resource);
        resources.get(i).setSalePrice(resource.getPricePerQuantity());
        resources.get(i).setDiscount(saleRequestDto.getResources().get(i).getDiscount());
        resources
            .get(i)
            .setQuantity(saleRequestDto.getResources().get(i).getResource().getQuantity());
      }
    }
    return resources;
  }
}
