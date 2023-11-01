package jewellery.inventory.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductPriceDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;

public class SaleTestHelper {
  public static Sale createSaleWithTodayDate(User seller, User buyer, List<Product> products) {
    Sale sale = new Sale();
    sale.setId(UUID.randomUUID());
    sale.setSeller(seller);
    sale.setBuyer(buyer);
    sale.setProducts(products);
    sale.setDate(new Date());
    sale.setDiscount(10.0);
    return sale;
  }

  public static SaleRequestDto createSaleRequest(UUID sellerId, UUID buyerId, List<ProductPriceDiscountRequestDto> products) {
    SaleRequestDto saleRequest = new SaleRequestDto();
    saleRequest.setSellerId(sellerId);
    saleRequest.setBuyerId(buyerId);
    saleRequest.setProducts(products);
    return saleRequest;
  }

  public static ProductPriceDiscountRequestDto createProductPriceDiscountRequest(UUID productId, double salePrice, double discount) {
    ProductPriceDiscountRequestDto productRequest = new ProductPriceDiscountRequestDto();
    productRequest.setProductId(productId);
    productRequest.setSalePrice(salePrice);
    productRequest.setDiscount(discount);
    return productRequest;
  }
  public static List<SaleResponseDto> getSaleResponseList(SaleResponseDto saleResponseDto){
    List<SaleResponseDto> saleResponseDtoList=new ArrayList<>();
    saleResponseDtoList.add(saleResponseDto);
    return saleResponseDtoList;
  }

  public static List<Product> getProList(Product product){
    List<Product> products=new ArrayList<>();
    products.add(product);
    return products;
  }
  public static SaleResponseDto getSaleResponseDto(Sale sale){
    SaleResponseDto dto=new SaleResponseDto();
    dto.setTotalDiscountedPrice(sale.getProducts().get(0).getSalePrice());
    dto.setTotalDiscount(sale.getProducts().get(0).getDiscount());
      return dto;
  }
}
