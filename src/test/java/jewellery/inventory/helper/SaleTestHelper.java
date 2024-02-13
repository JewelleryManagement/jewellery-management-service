package jewellery.inventory.helper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.ProductDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;

public class SaleTestHelper {
  public static Sale createSaleWithTodayDate(
      User seller, User buyer, List<ProductPriceDiscount> products) {
    Sale sale = new Sale();
    sale.setId(UUID.randomUUID());
    sale.setSeller(seller);
    sale.setBuyer(buyer);
    sale.setProducts(new ArrayList<>(products));
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
      UUID sellerId, UUID buyerId, List<ProductDiscountRequestDto> products) {
    SaleRequestDto saleRequest = new SaleRequestDto();
    saleRequest.setSellerId(sellerId);
    saleRequest.setBuyerId(buyerId);
    saleRequest.setProducts(products);
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
      Sale sale) {
    SaleResponseDto dto = new SaleResponseDto();
    UserResponseDto userResponseDtoSeller = createUserResponseDto(sale.getSeller());
    UserResponseDto userResponseDtoBuyer = createUserResponseDto(sale.getBuyer());

    dto.setSeller(userResponseDtoSeller);
    dto.setBuyer(userResponseDtoBuyer);
    ProductResponseDto productResponseDto = new ProductResponseDto();
    dto.setTotalDiscountedPrice(sale.getProducts().get(0).getSalePrice());
    dto.setTotalDiscount(BigDecimal.ZERO);
    dto.setProducts(List.of(productResponseDto));

    BigDecimal totalDiscountedPriceSum = sale.getProducts().stream()
            .map(product -> {
              BigDecimal salePrice = Optional.ofNullable(product.getSalePrice()).orElse(BigDecimal.ZERO);
              BigDecimal discount = Optional.ofNullable(product.getDiscount()).orElse(BigDecimal.ZERO);

              dto.setTotalDiscountedPrice(dto.getTotalDiscountedPrice().add(salePrice));
              dto.setTotalDiscount(dto.getTotalDiscount().add(discount));

              return dto.getTotalDiscountedPrice();
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    dto.setTotalPrice(totalDiscountedPriceSum);

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

  public static ProductPriceDiscount createTestProductPriceDiscount(Product product, Sale sale) {
    ProductPriceDiscount productPriceDiscount = new ProductPriceDiscount();
    productPriceDiscount.setDiscount(BigDecimal.ZERO);
    productPriceDiscount.setProduct(product);
    productPriceDiscount.setSale(sale);
    productPriceDiscount.setSalePrice(BigDecimal.ONE);
    return productPriceDiscount;
  }
}
