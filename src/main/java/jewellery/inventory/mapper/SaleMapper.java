package jewellery.inventory.mapper;

import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.PurchasedResourceInUserResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceReturnResponseDto;
import jewellery.inventory.model.ProductPriceDiscount;
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
  private final ResourceMapper resourceMapper;
  private static final String AMOUNT = "amount";
  private static final String PERCENTAGE = "percentage";

  public SaleResponseDto mapEntityToResponseDto(Sale sale) {
    SaleResponseDto saleResponseDto = new SaleResponseDto();
    saleResponseDto.setId(sale.getId());
    saleResponseDto.setSeller(userMapper.toUserResponse(sale.getSeller()));
    saleResponseDto.setBuyer(userMapper.toUserResponse(sale.getBuyer()));
    saleResponseDto.setProducts(mapAllProductsToResponse(sale));
    saleResponseDto.setResources(mapAllResourcesToResponse(sale));
    saleResponseDto.setTotalPrice(
        getTotalPriceFromEntities(sale.getProducts(), sale.getResources()));
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
      List<ProductPriceDiscount> products,
      List<PurchasedResourceInUser> resources) {
    Sale sale = new Sale();
    sale.setBuyer(buyer);
    sale.setSeller(seller);
    sale.setProducts(products);
    sale.setResources(resources);
    sale.setDate(saleRequestDto.getDate());
    return sale;
  }

  public ResourceReturnResponseDto mapToResourceReturnResponseDto(
      PurchasedResourceInUser resourceToReturn, SaleResponseDto sale) {
    return ResourceReturnResponseDto.builder()
        .returnedResource(resourceMapper.toResourceResponse(resourceToReturn.getResource()))
        .saleAfter(sale)
        .date(LocalDate.now())
        .build();
  }

  private List<ProductResponseDto> mapAllProductsToResponse(Sale sale) {
    if (sale.getProducts() != null) {
      return sale.getProducts().stream()
          .map(productSale -> productMapper.mapToProductResponseDto(productSale.getProduct()))
          .toList();
    }
    return new ArrayList<>();
  }

  public List<PurchasedResourceInUserResponseDto> mapAllResourcesToResponse(Sale sale) {
    List<PurchasedResourceInUserResponseDto> result = new ArrayList<>();
    if (sale.getResources() != null) {
      for (PurchasedResourceInUser resource : sale.getResources()) {
        result.add(getPurchasedResourceInUserResponseDto(sale, resource));
      }
    }
    return result;
  }

  private PurchasedResourceInUserResponseDto getPurchasedResourceInUserResponseDto(
      Sale sale, PurchasedResourceInUser resource) {
    return PurchasedResourceInUserResponseDto.builder()
        .resource(getResourceQuantityResponseDto(resource))
        .salePrice(resource.getSalePrice())
        .discount(resource.getDiscount())
        .owner(userMapper.toUserResponse(sale.getBuyer()))
        .build();
  }

  private ResourceQuantityResponseDto getResourceQuantityResponseDto(
      PurchasedResourceInUser resource) {
    return ResourceQuantityResponseDto.builder()
        .resource(resourceMapper.toResourceResponse(resource.getResource()))
        .quantity(resource.getQuantity())
        .build();
  }

  private BigDecimal getTotalPriceFromEntities(
      List<ProductPriceDiscount> productResponseDtoList, List<PurchasedResourceInUser> resources) {

    BigDecimal totalPrice =
        productResponseDtoList.stream()
            .map(ProductPriceDiscount::getSalePrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    totalPrice =
        resources.stream()
            .map(PurchasedResourceInUser::getSalePrice)
            .reduce(totalPrice, BigDecimal::add);

    return totalPrice;
  }

  private BigDecimal calculateDiscount(
      List<ProductPriceDiscount> products,
      List<PurchasedResourceInUser> resources,
      String calculationType) {
    BigDecimal totalDiscountAmount = calculateTotalDiscountAmount(products, resources);
    BigDecimal totalPrice = getTotalPriceFromEntities(products, resources);

    if (PERCENTAGE.equals(calculationType)) {
      return totalDiscountAmount
          .divide(totalPrice, 4, RoundingMode.HALF_UP)
          .multiply(getBigDecimal("100"));
    } else if (AMOUNT.equals(calculationType)) {
      return totalPrice.subtract(totalDiscountAmount);
    }
    throw new IllegalArgumentException("Invalid calculation type");
  }

  private BigDecimal calculateTotalDiscountAmount(
      List<ProductPriceDiscount> products, List<PurchasedResourceInUser> resources) {
    BigDecimal totalDiscountAmount = BigDecimal.ZERO;
    totalDiscountAmount = totalDiscountAmount.add(calculateDiscountForProducts(products));
    totalDiscountAmount = totalDiscountAmount.add(calculateDiscountForResources(resources));
    return totalDiscountAmount;
  }

  private BigDecimal calculateDiscountForProducts(List<ProductPriceDiscount> products) {
    BigDecimal discountAmount = BigDecimal.ZERO;
    for (ProductPriceDiscount product : products) {
      BigDecimal salePrice = product.getSalePrice();
      BigDecimal discountRate = product.getDiscount();
      discountAmount =
          discountAmount.add(salePrice.multiply(discountRate).divide(getBigDecimal("100")));
    }
    return discountAmount;
  }

  private BigDecimal calculateDiscountForResources(List<PurchasedResourceInUser> resources) {
    BigDecimal discountAmount = BigDecimal.ZERO;
    for (PurchasedResourceInUser resource : resources) {
      BigDecimal salePrice = Optional.ofNullable(resource.getSalePrice()).orElse(BigDecimal.ZERO);
      BigDecimal discountRate = Optional.ofNullable(resource.getDiscount()).orElse(BigDecimal.ZERO);
      discountAmount =
          discountAmount.add(
              salePrice.multiply(discountRate).divide(getBigDecimal("100"), RoundingMode.HALF_UP));
    }
    return discountAmount;
  }
}
