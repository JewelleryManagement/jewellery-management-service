package jewellery.inventory.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
      UUID productId, double salePrice, double discount) {
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

  public static SaleResponseDto getSaleResponseDto(Sale sale) {
    SaleResponseDto dto = new SaleResponseDto();
    UserResponseDto userResponseDtoSeller = new UserResponseDto();
    UserResponseDto userResponseDtoBuyer = new UserResponseDto();
    userResponseDtoSeller.setId(sale.getSeller().getId());
    dto.setSeller(userResponseDtoSeller);
    userResponseDtoBuyer.setId(sale.getBuyer().getId());
    dto.setBuyer(userResponseDtoBuyer);
    for (int i = 0; i < sale.getProducts().size(); i++) {
      dto.setTotalDiscountedPrice(
          dto.getTotalDiscountedPrice() + sale.getProducts().get(i).getSalePrice());
      dto.setTotalDiscount(dto.getTotalDiscount() + sale.getProducts().get(i).getDiscount());
    }
    List<ProductResponseDto> list = new ArrayList<>();
    for (int i = 0; i < sale.getProducts().size(); i++) {
      ProductResponseDto productResponseDto = new ProductResponseDto();
      productResponseDto.setOwner(dto.getBuyer());
      list.add(productResponseDto);
    }
    dto.setProducts(list);
    return dto;
  }
}
