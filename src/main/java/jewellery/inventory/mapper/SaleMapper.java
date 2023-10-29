package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SaleMapper {
  private final UserMapper userMapper;
  private final ProductMapper productMapper;

  public SaleResponseDto mapToSaleResponseDto(Sale sale) {
    SaleResponseDto saleResponseDto = new SaleResponseDto();
    saleResponseDto.setSeller(userMapper.toUserResponse(sale.getSeller()));
    saleResponseDto.setBuyer(userMapper.toUserResponse(sale.getBuyer()));
    saleResponseDto.setProducts(mapProducts(sale));
    saleResponseDto.setTotalPrice(0); //TODO
    saleResponseDto.setTotalDiscount(0);//TODO
    saleResponseDto.setTotalDiscountedPrice(0);//TODO
    return saleResponseDto;
  }

  private List<ProductResponseDto> mapProducts(Sale sale) {
    List<Product> products = sale.getProducts();
    List<ProductResponseDto> productResponseDtos = new ArrayList<>();
    for (int i = 0; i < products.size(); i++) {
      ProductResponseDto product = productMapper.mapToProductResponseDto(products.get(i));
      productResponseDtos.add(product);
    }
    return productResponseDtos;
  }
}
