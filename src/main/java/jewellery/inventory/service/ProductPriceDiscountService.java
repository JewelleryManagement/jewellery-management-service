package jewellery.inventory.service;

import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;
import jewellery.inventory.repository.ProductPriceDiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductPriceDiscountService {

  private final ProductPriceDiscountRepository productPriceDiscountRepository;
  private final ProductService productService;
  private final ProductMapper productMapper;

  public void createProductPriceDiscount(SaleRequestDto saleRequestDto, Sale sale) {
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

  public void deleteProductPriceDiscount(UUID saleId, UUID productId) {
    List<ProductPriceDiscount> list = productPriceDiscountRepository.findAll();
    for (ProductPriceDiscount productPriceDiscount : list) {
      if (productPriceDiscount.getProduct().getId().equals(productId)
          && productPriceDiscount.getSale().getId().equals(saleId)) {
        productPriceDiscountRepository.delete(productPriceDiscount);
      }
    }
  }
}
