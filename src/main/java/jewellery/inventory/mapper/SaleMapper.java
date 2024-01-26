package jewellery.inventory.mapper;

import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.PurchasedResourceInUserResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.dto.response.resource.ResourceReturnResponseDto;
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
  private final ResourceMapper resourceMapper;
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

  public ResourceReturnResponseDto mapToResourceReturnResponseDto(
      PurchasedResourceInUser resourceToReturn, SaleResponseDto sale) {
    return ResourceReturnResponseDto.builder()
        .returnedResource(resourceMapper.toResourceResponse(resourceToReturn.getResource()))
        .saleAfter(sale)
        .date(LocalDate.now())
        .build();
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
    List<PurchasedResourceInUserResponseDto> result = new ArrayList<>();
    for (PurchasedResourceInUser resource : sale.getResources()) {
      PurchasedResourceInUserResponseDto purchasedResourceInUserResponseDto =
          new PurchasedResourceInUserResponseDto();
      ResourceQuantityResponseDto resourceQuantityResponseDto = new ResourceQuantityResponseDto();
      ResourceResponseDto resourceResponseDto =
          resourceMapper.toResourceResponse(resource.getResource());
      resourceQuantityResponseDto.setResource(resourceResponseDto);
      resourceQuantityResponseDto.setQuantity(resource.getQuantity());
      purchasedResourceInUserResponseDto.setResource(resourceQuantityResponseDto);
      purchasedResourceInUserResponseDto.setSalePrice(resource.getSalePrice());
      purchasedResourceInUserResponseDto.setDiscount(resource.getDiscount());
      result.add(purchasedResourceInUserResponseDto);
    }
    return result;
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
    BigDecimal totalDiscountAmount = calculateTotalDiscountAmount(products, resources);
    BigDecimal totalPrice = calculateTotalPrice(products, resources);

    if (PERCENTAGE.equals(calculationType) && !totalPrice.equals(BigDecimal.ZERO)) {
      return totalDiscountAmount
          .divide(totalPrice, MathContext.DECIMAL128)
          .multiply(getBigDecimal("100"));
    } else if (AMOUNT.equals(calculationType)) {
      return totalPrice.subtract(totalDiscountAmount);
    }

    throw new IllegalArgumentException("Invalid calculation type");
  }

  private BigDecimal calculateTotalDiscountAmount(
      List<Product> products, List<PurchasedResourceInUser> resources) {
    BigDecimal totalDiscountAmount = BigDecimal.ZERO;
    totalDiscountAmount = totalDiscountAmount.add(calculateDiscountForProducts(products));
    totalDiscountAmount = totalDiscountAmount.add(calculateDiscountForResources(resources));
    return totalDiscountAmount;
  }

  private BigDecimal calculateTotalPrice(
      List<Product> products, List<PurchasedResourceInUser> resources) {
    BigDecimal totalPrice = BigDecimal.ZERO;
    totalPrice = totalPrice.add(calculatePriceForProducts(products));
    totalPrice = totalPrice.add(calculatePriceForResources(resources));
    return totalPrice;
  }

  private BigDecimal calculateDiscountForProducts(List<Product> products) {
    BigDecimal discountAmount = BigDecimal.ZERO;
    for (Product product : products) {
      BigDecimal salePrice = Optional.ofNullable(product.getSalePrice()).orElse(BigDecimal.ZERO);
      BigDecimal discountRate = Optional.ofNullable(product.getDiscount()).orElse(BigDecimal.ZERO);
      discountAmount =
          discountAmount.add(
              salePrice.multiply(discountRate.divide(getBigDecimal("100"), RoundingMode.HALF_UP)));
    }
    return discountAmount;
  }

  private BigDecimal calculateDiscountForResources(List<PurchasedResourceInUser> resources) {
    BigDecimal discountAmount = BigDecimal.ZERO;
    for (PurchasedResourceInUser resource : resources) {
      BigDecimal salePrice =
          Optional.ofNullable(resource.getResource().getPricePerQuantity()).orElse(BigDecimal.ZERO);
      BigDecimal discountRate = Optional.ofNullable(resource.getDiscount()).orElse(BigDecimal.ZERO);
      discountAmount =
          discountAmount.add(
              salePrice.multiply(discountRate.divide(getBigDecimal("100"), RoundingMode.HALF_UP)));
    }
    return discountAmount;
  }

  private BigDecimal calculatePriceForProducts(List<Product> products) {
    BigDecimal totalPrice = BigDecimal.ZERO;
    for (Product product : products) {
      totalPrice =
          totalPrice.add(Optional.ofNullable(product.getSalePrice()).orElse(BigDecimal.ZERO));
    }
    return totalPrice;
  }

  private BigDecimal calculatePriceForResources(List<PurchasedResourceInUser> resources) {
    BigDecimal totalPrice = BigDecimal.ZERO;
    for (PurchasedResourceInUser resource : resources) {
      totalPrice =
          totalPrice.add(
              Optional.ofNullable(resource.getResource().getPricePerQuantity())
                  .orElse(BigDecimal.ZERO));
    }
    return totalPrice;
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
