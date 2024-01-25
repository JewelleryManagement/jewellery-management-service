package jewellery.inventory.helper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.ProductPriceDiscountRequestDto;
import jewellery.inventory.dto.request.PurchasedResourceInUserRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.model.*;
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
      User seller, User buyer, List<Product> products, List<PurchasedResourceInUser> resources) {
    Sale sale = new Sale();
    sale.setId(UUID.randomUUID());
    sale.setSeller(seller);
    sale.setBuyer(buyer);
    sale.setProducts(products);
    sale.setResources(resources);
    sale.setDate(LocalDate.now());
    return sale;
  }

  public static SaleRequestDto createSaleRequest(
      UUID sellerId,
      UUID buyerId,
      List<ProductPriceDiscountRequestDto> products,
      List<PurchasedResourceInUserRequestDto> resources) {
    SaleRequestDto saleRequest = new SaleRequestDto();
    saleRequest.setSellerId(sellerId);
    saleRequest.setBuyerId(buyerId);
    saleRequest.setProducts(products);
    saleRequest.setResources(resources);
    return saleRequest;
  }

  public static ProductPriceDiscountRequestDto createProductPriceDiscountRequest(
      UUID productId, BigDecimal salePrice, BigDecimal discount) {
    ProductPriceDiscountRequestDto productRequest = new ProductPriceDiscountRequestDto();
    productRequest.setProductId(productId);
    productRequest.setSalePrice(salePrice);
    productRequest.setDiscount(discount);
    return productRequest;
  }

  public static List<Product> getProductsList(Product product) {
    List<Product> products = new ArrayList<>();
    products.add(product);
    return products;
  }

  public static List<Product> getProductsList(Product product, Product otherProduct) {
    List<Product> products = new ArrayList<>();
    products.add(product);
    otherProduct.setId(UUID.randomUUID());
    products.add(otherProduct);
    return products;
  }

  public static SaleResponseDto getSaleResponseDto(Sale sale) {
    SaleResponseDto dto = new SaleResponseDto();
    UserResponseDto userResponseDtoSeller = createUserResponseDto(sale.getSeller());
    UserResponseDto userResponseDtoBuyer = createUserResponseDto(sale.getBuyer());

    dto.setSeller(userResponseDtoSeller);
    dto.setBuyer(userResponseDtoBuyer);
    dto.setTotalDiscountedPrice(BigDecimal.ZERO);
    dto.setTotalDiscount(BigDecimal.ZERO);

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
        .quantityType("carat")
        .pricePerQuantity(price)
        .clazz("Pearl")
        .build();
  }

  private static ResourceResponseDto createResourceResponseDto() {
    return ResourceResponseDto.builder()
        .id(UUID.randomUUID())
        .clazz("Pearl")
        .quantityType("carat")
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
}
