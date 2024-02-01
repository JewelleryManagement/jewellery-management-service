package jewellery.inventory.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

  public List<ProductPriceDiscount> createProductPriceDiscount(
      SaleRequestDto saleRequestDto, Sale sale) {
    List<ProductPriceDiscount> list = new ArrayList<>();
    if (saleRequestDto.getProducts() != null) {
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
        list.add(productPriceDiscount);
      }
      return list;
    }
    return new ArrayList<>();
  }

  public void deleteProductPriceDiscount(UUID saleId, UUID productId) {
    ProductPriceDiscount productPriceDiscountForDelete =
        productPriceDiscountRepository.findBySaleIdAndProductId(saleId, productId);
    productPriceDiscountRepository.delete(productPriceDiscountForDelete);
  }
}
