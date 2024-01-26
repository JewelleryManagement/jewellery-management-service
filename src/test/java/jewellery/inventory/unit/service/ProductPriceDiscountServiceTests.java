package jewellery.inventory.unit.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductPriceDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;
import jewellery.inventory.repository.ProductPriceDiscountRepository;
import jewellery.inventory.service.ProductPriceDiscountService;
import jewellery.inventory.service.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductPriceDiscountServiceTests {
  @InjectMocks
  private ProductPriceDiscountService productPriceDiscountService;
  @Mock
  private ProductPriceDiscountRepository productPriceDiscountRepository;

  @Mock
  private ProductService productService;

  @Mock
  private ProductMapper productMapper;

  @Test
  public void testDeleteProductPriceDiscount() {
    UUID saleId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    ProductPriceDiscount productPriceDiscount = new ProductPriceDiscount();

    when(productPriceDiscountRepository.findBySaleIdAndProductId(saleId, productId))
            .thenReturn(productPriceDiscount);

    productPriceDiscountService.deleteProductPriceDiscount(saleId, productId);

     verify(productPriceDiscountRepository, times(1)).delete(productPriceDiscount);
  }
  @Test
  public void testCreateProductPriceDiscount() {
    SaleRequestDto saleRequestDto = new SaleRequestDto();
    saleRequestDto.setProducts(List.of(new ProductPriceDiscountRequestDto()));
    Sale sale = new Sale();

    lenient().when(productService.getProduct(any(UUID.class))).thenReturn(new Product());

    when(productMapper.mapToProductResponseDto(any())).thenReturn(new ProductResponseDto());

    List<ProductPriceDiscount> result =
            productPriceDiscountService.createProductPriceDiscount(saleRequestDto, sale);

    assertEquals(saleRequestDto.getProducts().size(), result.size());
     verify(productPriceDiscountRepository, times(saleRequestDto.getProducts().size())).save(any());
  }

}