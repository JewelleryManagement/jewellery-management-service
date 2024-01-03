package jewellery.inventory.helper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.ProductPriceDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;

public class SaleTestHelper {

  public static ProductReturnResponseDto getProductReturnResponseDto(
      SaleResponseDto sale, ProductResponseDto product) {
    ProductReturnResponseDto productReturn = new ProductReturnResponseDto();
    productReturn.setSaleAfter(sale);
    productReturn.setReturnedProduct(product);
    productReturn.setDate(LocalDate.now());
    return productReturn;
  }

  public static Sale createSaleWithTodayDate(User seller, User buyer, List<Product> products) {
    Sale sale = new Sale();
    sale.setId(UUID.randomUUID());
    sale.setSeller(seller);
    sale.setBuyer(buyer);
    sale.setProducts(products);
    sale.setDate(LocalDate.now());
    return sale;
  }

  public static SaleRequestDto createSaleRequest(
      UUID sellerId, UUID buyerId, List<ProductPriceDiscountRequestDto> products) {
    SaleRequestDto saleRequest = new SaleRequestDto();
    saleRequest.setSellerId(sellerId);
    saleRequest.setBuyerId(buyerId);
    saleRequest.setProducts(products);
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
    dto.setTotalDiscountedPrice(new BigDecimal("0"));
    dto.setTotalDiscount(new BigDecimal("0"));

    sale.getProducts()
        .forEach(
            product -> {
              BigDecimal salePrice =
                  Optional.ofNullable(product.getSalePrice()).orElse(new BigDecimal("0"));
              BigDecimal discount =
                  Optional.ofNullable(product.getDiscount()).orElse(new BigDecimal("0"));
              dto.setTotalDiscountedPrice(dto.getTotalDiscountedPrice().add(salePrice));
              dto.setTotalDiscount(dto.getTotalDiscount().add(discount));
            });

    List<ProductResponseDto> productResponseDtos =
        sale.getProducts().stream()
            .map(product -> createProductResponseDto(dto.getBuyer()))
            .collect(Collectors.toList());

    dto.setProducts(productResponseDtos);

    return dto;
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
}
