package jewellery.inventory.helper;

import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.ProductDiscountRequestDto;
import jewellery.inventory.dto.request.PurchasedResourceQuantityRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.model.*;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import org.jetbrains.annotations.NotNull;

public class SaleTestHelper {

  public static Sale createSaleWithTodayDate(
      User seller,
      User buyer,
      List<ProductPriceDiscount> products,
      List<PurchasedResourceInUser> resources) {
    Sale sale = new Sale();
    sale.setId(UUID.randomUUID());
    sale.setSeller(seller);
    sale.setBuyer(buyer);
    sale.setProducts(new ArrayList<>(products));
    sale.setResources(new ArrayList<>(resources));
    sale.setDate(LocalDate.now());
    return sale;
  }

  public static ProductReturnResponseDto createProductReturnResponseDto(
      Product product, User user) {
    ProductReturnResponseDto dto = new ProductReturnResponseDto();
    ProductResponseDto productResponseDto = new ProductResponseDto();
    productResponseDto.setId(product.getId());
    productResponseDto.setPartOfSale(null);
    productResponseDto.setOwner(createUserResponseDto(user));
    dto.setReturnedProduct(productResponseDto);
    dto.setSaleAfter(null);
    dto.setDate(LocalDate.now());
    return dto;
  }

  public static SaleRequestDto createSaleRequest(
      UUID sellerId,
      UUID buyerId,
      List<ProductDiscountRequestDto> products,
      List<PurchasedResourceQuantityRequestDto> resources) {
    SaleRequestDto saleRequest = new SaleRequestDto();
    saleRequest.setSellerId(sellerId);
    saleRequest.setBuyerId(buyerId);
    saleRequest.setProducts(products);
    saleRequest.setResources(resources);
    saleRequest.setDate(LocalDate.now());
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
      Sale sale, BigDecimal salePrice, BigDecimal discount) {
    SaleResponseDto dto = new SaleResponseDto();
    UserResponseDto userResponseDtoSeller = createUserResponseDto(sale.getSeller());
    UserResponseDto userResponseDtoBuyer = createUserResponseDto(sale.getBuyer());

    dto.setSeller(userResponseDtoSeller);
    dto.setBuyer(userResponseDtoBuyer);
    ProductResponseDto productResponseDto = new ProductResponseDto();
    dto.setTotalDiscountedPrice(sale.getProducts().get(0).getSalePrice());
    dto.setTotalDiscount(BigDecimal.ZERO);
    dto.setProducts(List.of(productResponseDto));

    calculateProductsDiscounts(sale, dto, salePrice, discount);
    calculateResourcesDiscounts(sale, dto, salePrice, discount);

    dto.setProducts(createProductsResponse(sale, dto));
    dto.setResources(createResourcesResponse(sale));

    return dto;
  }

  public static OrganizationSaleResponseDto getOrganizationSaleResponseDto(
      Sale sale, BigDecimal salePrice, BigDecimal discount) {
    OrganizationSaleResponseDto dto = new OrganizationSaleResponseDto();
    OrganizationResponseDto organizationResponseDto =
        OrganizationTestHelper.getTestOrganizationResponseDto(
            OrganizationTestHelper.getTestOrganization());
    UserResponseDto userResponseDtoBuyer = createUserResponseDto(sale.getBuyer());

    dto.setOrganizationSeller(organizationResponseDto);
    dto.setBuyer(userResponseDtoBuyer);
    ProductResponseDto productResponseDto = new ProductResponseDto();
    dto.setTotalDiscountedPrice(sale.getProducts().get(0).getSalePrice());
    dto.setTotalDiscount(BigDecimal.ZERO);
    dto.setProducts(List.of(productResponseDto));

    calculateOrganizationProductsDiscounts(sale, dto, salePrice, discount);
    calculateOrganizationResourcesDiscounts(sale, dto, salePrice, discount);

    dto.setProducts(createOrganizationProductsResponse(sale, dto));
    dto.setResources(createResourcesResponse(sale));

    return dto;
  }

  @NotNull
  private static List<ProductResponseDto> createProductsResponse(Sale sale, SaleResponseDto dto) {
    return sale.getProducts().stream()
        .map(product -> createProductResponseDto(dto.getBuyer()))
        .collect(Collectors.toList());
  }

  @NotNull
  private static List<ProductResponseDto> createOrganizationProductsResponse(
      Sale sale, OrganizationSaleResponseDto dto) {
    return sale.getProducts().stream()
        .map(product -> createProductResponseDto(dto.getBuyer()))
        .collect(Collectors.toList());
  }

  private static List<PurchasedResourceQuantityResponseDto> createResourcesResponse(Sale sale) {
    if (sale.getResources() != null) {
      return sale.getResources().stream()
          .map(resource -> createPurchasedResourceResponseDto())
          .toList();
    }
    return null;
  }

  private static void calculateResourcesDiscounts(
      Sale sale, SaleResponseDto dto, BigDecimal price, BigDecimal discount) {
    sale.getResources()
        .forEach(
            resource -> {
              BigDecimal salePrice = Optional.ofNullable(resource.getSalePrice()).orElse(price);
              BigDecimal saleDiscount =
                  Optional.ofNullable(resource.getDiscount()).orElse(discount);
              dto.setTotalDiscountedPrice(dto.getTotalDiscountedPrice().add(salePrice));
              dto.setTotalDiscount(
                  (dto.getTotalDiscount().add(saleDiscount))
                      .divide(salePrice, MathContext.DECIMAL128)
                      .multiply(getBigDecimal("100")));
            });
  }

  private static void calculateOrganizationResourcesDiscounts(
      Sale sale, OrganizationSaleResponseDto dto, BigDecimal price, BigDecimal discount) {
    sale.getResources()
        .forEach(
            resource -> {
              BigDecimal salePrice = Optional.ofNullable(resource.getSalePrice()).orElse(price);
              BigDecimal saleDiscount =
                  Optional.ofNullable(resource.getDiscount()).orElse(discount);
              dto.setTotalDiscountedPrice(dto.getTotalDiscountedPrice().add(salePrice));
              dto.setTotalDiscount(
                  (dto.getTotalDiscount().add(saleDiscount))
                      .divide(salePrice, MathContext.DECIMAL128)
                      .multiply(getBigDecimal("100")));
            });
  }

  private static void calculateProductsDiscounts(
      Sale sale, SaleResponseDto dto, BigDecimal price, BigDecimal discount) {
    sale.getProducts()
        .forEach(
            product -> {
              BigDecimal salePrice = Optional.ofNullable(product.getSalePrice()).orElse(price);
              BigDecimal saleDiscount = Optional.ofNullable(product.getDiscount()).orElse(discount);
              dto.setTotalDiscountedPrice(dto.getTotalDiscountedPrice().add(salePrice));
              dto.setTotalDiscount(
                  (dto.getTotalDiscount().add(saleDiscount))
                      .divide(salePrice, MathContext.DECIMAL128)
                      .multiply(getBigDecimal("100")));
            });
  }

  private static void calculateOrganizationProductsDiscounts(
      Sale sale, OrganizationSaleResponseDto dto, BigDecimal price, BigDecimal discount) {
    sale.getProducts()
        .forEach(
            product -> {
              BigDecimal salePrice = Optional.ofNullable(product.getSalePrice()).orElse(price);
              BigDecimal saleDiscount = Optional.ofNullable(product.getDiscount()).orElse(discount);
              dto.setTotalDiscountedPrice(dto.getTotalDiscountedPrice().add(salePrice));
              dto.setTotalDiscount(
                  (dto.getTotalDiscount().add(saleDiscount))
                      .divide(salePrice, MathContext.DECIMAL128)
                      .multiply(getBigDecimal("100")));
            });
  }

  public static PurchasedResourceInUser createPurchasedResource(BigDecimal price) {
    return PurchasedResourceInUser.builder()
        .id(UUID.randomUUID())
        .salePrice(getBigDecimal("100"))
        .resource(ResourceTestHelper.getPearl(price))
        .discount(getBigDecimal("10"))
        .quantity(getBigDecimal("5"))
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

  private static ResourceQuantityResponseDto createResourceQuantityResponseDto() {
    ResourceResponseDto resourceResponseDto = ResourceTestHelper.getPearlResponseDto();
    resourceResponseDto.setPricePerQuantity(getBigDecimal("20"));
    return ResourceQuantityResponseDto.builder()
        .resource(resourceResponseDto)
        .quantity(getBigDecimal("10"))
        .build();
  }

  public static PurchasedResourceQuantityResponseDto createPurchasedResourceResponseDto() {
    return PurchasedResourceQuantityResponseDto.builder()
        .resourceAndQuantity(createResourceQuantityResponseDto())
        .discount(getBigDecimal("10"))
        .salePrice(getBigDecimal("100"))
        .build();
  }

  private static ResourceQuantityRequestDto createResourceQuantityRequest() {
    ResourceQuantityRequestDto requestDto = new ResourceQuantityRequestDto();
    requestDto.setResourceId(ResourceTestHelper.getPearl(getBigDecimal("20")).getId());
    requestDto.setQuantity(getBigDecimal("10"));
    return requestDto;
  }

  public static PurchasedResourceQuantityRequestDto createPurchasedResourceRequestDto() {
    return PurchasedResourceQuantityRequestDto.builder()
        .resourceAndQuantity(createResourceQuantityRequest())
        .discount(getBigDecimal("10"))
        .build();
  }

  public static ResourceInUser createResourceInUser(BigDecimal price) {
    ResourceInUser resourceInUser = new ResourceInUser();
    resourceInUser.setResource(ResourceTestHelper.getPearl(price));
    resourceInUser.setQuantity(BigDecimal.TEN);
    resourceInUser.setOwner(UserTestHelper.createTestUserWithId());
    resourceInUser.setId(UUID.randomUUID());
    resourceInUser.setDealPrice(getBigDecimal("500"));
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

  public static Sale createSaleInOrganization(
      Organization seller,
      User buyer,
      List<ProductPriceDiscount> products,
      List<PurchasedResourceInUser> resources) {
    Sale sale = new Sale();
    sale.setId(UUID.randomUUID());
    sale.setOrganizationSeller(seller);
    sale.setBuyer(buyer);
    sale.setProducts(new ArrayList<>(products));
    sale.setResources(new ArrayList<>(resources));
    sale.setDate(LocalDate.now());
    return sale;
  }

  public static SaleRequestDto getSaleInOrganizationRequestDto(
          Organization seller,
          User buyer,
          ProductsInOrganizationResponseDto productsInOrganizationResponseDto,
          ResourcesInOrganizationResponseDto resourcesInOrganizationResponseDto,
          BigDecimal saleDiscount) {
    SaleRequestDto saleRequestDto = new SaleRequestDto();
    saleRequestDto.setBuyerId(buyer.getId());
    saleRequestDto.setSellerId(seller.getId());
    saleRequestDto.setDate(LocalDate.now());
    PurchasedResourceQuantityRequestDto purchasedResourceQuantityRequestDto =
            new PurchasedResourceQuantityRequestDto();
    ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
    resourceQuantityRequestDto.setResourceId(
            resourcesInOrganizationResponseDto
                    .getResourcesAndQuantities()
                    .get(0)
                    .getResource()
                    .getId());
    resourceQuantityRequestDto.setQuantity(BigDecimal.ONE);
    purchasedResourceQuantityRequestDto.setResourceAndQuantity(resourceQuantityRequestDto);
    purchasedResourceQuantityRequestDto.setDiscount(saleDiscount);
    List<PurchasedResourceQuantityRequestDto> resources = new ArrayList<>();
    resources.add(purchasedResourceQuantityRequestDto);
    saleRequestDto.setResources(resources);
    ProductDiscountRequestDto productDiscountRequestDto = new ProductDiscountRequestDto();
    productDiscountRequestDto.setProductId(
            productsInOrganizationResponseDto.getProducts().get(0).getId());
    productDiscountRequestDto.setDiscount(saleDiscount);
    List<ProductDiscountRequestDto> list = new ArrayList<>();
    list.add(productDiscountRequestDto);
    saleRequestDto.setProducts(list);
    return saleRequestDto;
  }
}
