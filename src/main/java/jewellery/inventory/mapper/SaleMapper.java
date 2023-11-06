package jewellery.inventory.mapper;

import java.time.Instant;
import java.util.*;
import jewellery.inventory.dto.request.ProductPriceDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaleMapper {
  private final UserMapper userMapper;
  private final ProductMapper productMapper;

  public SaleResponseDto mapEntityToResponseDto(Sale sale) {
    SaleResponseDto saleResponseDto = new SaleResponseDto();
    saleResponseDto.setSeller(userMapper.toUserResponse(sale.getSeller()));
    saleResponseDto.setBuyer(userMapper.toUserResponse(sale.getBuyer()));
    saleResponseDto.setProducts(mapAllProductsToResponse(sale));
    saleResponseDto.setTotalPrice(getTotalPriceFromEntity(sale.getProducts()));
    saleResponseDto.setTotalDiscount(getTotalDiscountPercentageFromEntity(sale.getProducts()));
    saleResponseDto.setTotalDiscountedPrice(getTotalDiscountFromEntity(sale.getProducts()));
    return saleResponseDto;
  }

  public Sale mapRequestToEntity(
      SaleRequestDto saleRequestDto, User seller, User buyer, List<Product> products) {
    Sale sale = new Sale();
    sale.setBuyer(buyer);
    sale.setSeller(seller);
    sale.setProducts(setProductPriceAndDiscount(saleRequestDto, products));
    sale.setDate(Date.from(Instant.now()));
    sale.setDiscount(getDiscountInPercentage(saleRequestDto.getProducts()));
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

  private double getDiscountInPercentage(List<ProductPriceDiscountRequestDto> discountList) {
    double discountPercentage = 0;
    for (ProductPriceDiscountRequestDto productPriceDiscountRequestDto : discountList) {
      discountPercentage += productPriceDiscountRequestDto.getDiscount();
    }
    return discountPercentage / discountList.size();
  }

  private double getTotalPriceFromEntity(List<Product> products) {
    double totalPrice = 0;
    for (Product product : products) {
      totalPrice += product.getSalePrice();
    }
    return totalPrice;
  }

  private double getTotalDiscountPercentageFromEntity(List<Product> products) {
    double totalDiscountAmount = 0;
    double totalPrice = 0;

    for (Product product : products) {
      double discountAmount = product.getSalePrice() * (product.getDiscount() / 100);
      totalDiscountAmount += discountAmount;
      totalPrice += product.getSalePrice();
    }
    if (totalPrice != 0) {
      return (totalDiscountAmount / totalPrice) * 100;
    } else {
      return 0;
    }
  }

  private double getTotalDiscountFromEntity(List<Product> products) {
    double totalDiscountAmount = 0;
    double totalPrice = 0;

    for (Product product : products) {
      double discountAmount = product.getSalePrice() * (product.getDiscount() / 100);
      totalDiscountAmount += discountAmount;
      totalPrice += product.getSalePrice();
    }
    return (products.isEmpty() ? 0 : totalPrice - totalDiscountAmount);
  }

  private List<Product> setProductPriceAndDiscount(
      SaleRequestDto saleRequestDto, List<Product> products) {
    for (int i = 0; i < products.size(); i++) {
      products.get(i).setSalePrice(saleRequestDto.getProducts().get(i).getSalePrice());
      products.get(i).setDiscount(saleRequestDto.getProducts().get(i).getDiscount());
    }
    return products;
  }
}
