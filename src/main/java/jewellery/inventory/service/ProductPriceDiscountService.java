package jewellery.inventory.service;

import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;
import jewellery.inventory.repository.ProductPriceDiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductPriceDiscountService {

  private final ProductPriceDiscountRepository productPriceDiscountRepository;
  private final ProductService productService;
  private final ProductMapper productMapper;

  public void crateProductPriceDiscount(SaleRequestDto saleRequestDto, Sale sale) {
    for (int i = 0; i < saleRequestDto.getProducts().size(); i++) {
      ProductPriceDiscount productPriceDiscount = new ProductPriceDiscount();
      Product product =
          productService.getProduct(saleRequestDto.getProducts().get(i).getProductId());
      productPriceDiscount.setProduct(product);
      productPriceDiscount.setDiscount(saleRequestDto.getProducts().get(i).getDiscount());
      productPriceDiscount.setSale(sale);
      productPriceDiscount.setSalePrice(
          productMapper.mapToProductResponseDto(product).getSalePrice());
      productPriceDiscountRepository.save(productPriceDiscount);
    }
  }
}
