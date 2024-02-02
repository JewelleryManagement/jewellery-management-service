package jewellery.inventory.helper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.ProductDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.model.*;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;

public class SaleTestHelper {

  public static ProductReturnResponseDto getProductReturnResponseDto(
      SaleResponseDto sale, ProductResponseDto product) {
    ProductReturnResponseDto productReturn = new ProductReturnResponseDto();
    productReturn.setSaleAfter(sale);
    productReturn.setReturnedProduct(product);
    productReturn.setDate(LocalDate.now());
    return productReturn;
  }

  public static Sale createSaleWithTodayDate(
      User seller, User buyer, List<PurchasedResourceInUser> resources) {
    Sale sale = new Sale();
    sale.setId(UUID.randomUUID());
    sale.setSeller(seller);
    sale.setBuyer(buyer);
    sale.setResources(resources);
    sale.setDate(LocalDate.now());
    return sale;
  }

  public static SaleRequestDto createSaleRequest(
      UUID sellerId,
      UUID buyerId,
      List<ProductDiscountRequestDto> products,
      List<PurchasedResourceInUserRequestDto> resources) {
    SaleRequestDto saleRequest = new SaleRequestDto();
    saleRequest.setSellerId(sellerId);
    saleRequest.setBuyerId(buyerId);
    saleRequest.setProducts(products);
    saleRequest.setResources(resources);
    return saleRequest;
  }

  public static ProductDiscountRequestDto createProductPriceDiscountRequest(
      UUID productId, BigDecimal discount) {
    ProductDiscountRequestDto productRequest = new ProductDiscountRequestDto();
    productRequest.setProductId(productId);
    productRequest.setDiscount(discount);
    return productRequest;
  }

  public static SaleResponseDto getSaleResponseDto(
      Sale sale, ProductPriceDiscount productPriceDiscount) {
    SaleResponseDto dto = new SaleResponseDto();
    UserResponseDto userResponseDtoSeller = createUserResponseDto(sale.getSeller());
    UserResponseDto userResponseDtoBuyer = createUserResponseDto(sale.getBuyer());

    dto.setSeller(userResponseDtoSeller);
    dto.setBuyer(userResponseDtoBuyer);
    ProductResponseDto productResponseDto = new ProductResponseDto();
    productResponseDto.setId(productPriceDiscount.getId());
    dto.setTotalPrice(productPriceDiscount.getSalePrice());
    dto.setTotalDiscountedPrice(productPriceDiscount.getSalePrice());
    dto.setTotalDiscount(BigDecimal.ZERO);
    dto.setProducts(List.of(productResponseDto));

    sale.getProducts()
        .forEach(
            product -> {
              BigDecimal salePrice =
                  Optional.ofNullable(product.getSalePrice()).orElse(BigDecimal.ZERO);
              BigDecimal discount =
                  Optional.ofNullable(product.getDiscount()).orElse(BigDecimal.ZERO);
              dto.setTotalDiscountedPrice(dto.getTotalDiscountedPrice().add(salePrice));
              dto.setTotalDiscount(dto.getTotalDiscount().add(discount));
            });

    sale.getResources()
        .forEach(
            resource -> {
              BigDecimal salePrice =
                  Optional.ofNullable(resource.getSalePrice()).orElse(BigDecimal.ZERO);
              BigDecimal discount =
                  Optional.ofNullable(resource.getDiscount()).orElse(BigDecimal.ZERO);
              dto.setTotalDiscountedPrice(dto.getTotalDiscountedPrice().add(salePrice));
              dto.setTotalDiscount(dto.getTotalDiscount().add(discount));
            });

    List<ProductResponseDto> productResponseDtos =
        sale.getProducts().stream()
            .map(product -> createProductResponseDto(dto.getBuyer()))
            .collect(Collectors.toList());

    dto.setProducts(productResponseDtos);

    List<PurchasedResourceInUserResponseDto> resourcesResponse =
        sale.getResources().stream()
            .map(resource -> createPurchasedResourceResponseDto(sale))
            .toList();

    dto.setResources(resourcesResponse);

    return dto;
  }

  public static PurchasedResourceInUser createPurchasedResource(BigDecimal price) {
    return PurchasedResourceInUser.builder()
        .id(UUID.randomUUID())
        .salePrice(BigDecimal.TEN)
        .resource(createResource(price))
        .discount(BigDecimal.TEN)
        .quantity(BigDecimal.ONE)
        .build();
  }

  private static UserResponseDto createUserResponseDto(User user) {
    UserResponseDto userResponseDto = new UserResponseDto();
    userResponseDto.setId(user.getId());
    return userResponseDto;
  }

  private static ProductResponseDto createProductResponseDto(UserResponseDto owner) {
    ProductResponseDto productResponseDto = new ProductResponseDto();
    productResponseDto.setOwner(owner);
    return productResponseDto;
  }

  public static Resource createResource(BigDecimal price) {
    return Resource.builder()
        .id(UUID.randomUUID())
        .quantityType("Gram")
        .pricePerQuantity(price)
        .clazz("Pearl")
        .build();
  }

  private static ResourceResponseDto createResourceResponseDto() {
    return ResourceResponseDto.builder()
        .id(UUID.randomUUID())
        .clazz("Pearl")
        .quantityType("Gram")
        .pricePerQuantity(BigDecimal.TEN)
        .build();
  }

  private static ResourceQuantityResponseDto createResourceQuantityResponseDto() {
    return ResourceQuantityResponseDto.builder()
        .resource(createResourceResponseDto())
        .quantity(BigDecimal.TEN)
        .build();
  }

  public static PurchasedResourceInUserResponseDto createPurchasedResourceResponseDto(Sale sale) {
    return PurchasedResourceInUserResponseDto.builder()
        .resource(createResourceQuantityResponseDto())
        .discount(BigDecimal.TEN)
        .salePrice(BigDecimal.ONE)
        .build();
  }

  private static ResourceQuantityRequestDto createResourceQuantityRequest() {
    ResourceQuantityRequestDto requestDto = new ResourceQuantityRequestDto();
    requestDto.setResourceId(createResource(BigDecimal.TEN).getId());
    requestDto.setQuantity(BigDecimal.TEN);
    return requestDto;
  }

  public static PurchasedResourceInUserRequestDto createPurchasedResourceRequestDto() {
    return PurchasedResourceInUserRequestDto.builder()
        .resource(createResourceQuantityRequest())
        .discount(BigDecimal.TEN)
        .build();
  }

  public static ResourceInUser createResourceInUser(BigDecimal price) {
    ResourceInUser resourceInUser = new ResourceInUser();
    resourceInUser.setResource(createResource(price));
    resourceInUser.setQuantity(BigDecimal.TEN);
    resourceInUser.setOwner(UserTestHelper.createTestUserWithId());
    resourceInUser.setId(UUID.randomUUID());
    resourceInUser.setDealPrice(BigDecimal.TEN);
    return resourceInUser;
  }

  public static ProductPriceDiscount createTestProductPriceDiscount(Product product, Sale sale) {
    ProductPriceDiscount productPriceDiscount = new ProductPriceDiscount();
    productPriceDiscount.setDiscount(BigDecimal.ZERO);
    productPriceDiscount.setProduct(product);
    productPriceDiscount.setSale(sale);
    productPriceDiscount.setSalePrice(BigDecimal.ONE);
    return productPriceDiscount;
  }
}
