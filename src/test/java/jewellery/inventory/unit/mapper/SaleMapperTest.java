package jewellery.inventory.unit.mapper;

import static jewellery.inventory.helper.ProductTestHelper.getTestProduct;
import static jewellery.inventory.helper.UserTestHelper.createSecondTestUser;
import static jewellery.inventory.helper.UserTestHelper.createTestUserForSale;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import jewellery.inventory.dto.request.ProductPriceDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.helper.SaleTestHelper;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaleMapperTest {
  @Mock private SaleMapper saleMapper;
  private User seller;
  private User buyer;
  private Product product;
  private Sale sale;
  private SaleRequestDto saleRequestDto;
  private SaleResponseDto saleResponseDto;
  private ProductPriceDiscountRequestDto productPriceDiscountRequestDto;
  private List<Product> productsForSale;

  @BeforeEach
  void setUp() {
    seller = createTestUserForSale();
    buyer = createSecondTestUser();
    product = getTestProduct(seller, new Resource());
    productsForSale = SaleTestHelper.getProductsList(product);
    sale = SaleTestHelper.createSaleWithTodayDate(seller, buyer, productsForSale);
    saleResponseDto = SaleTestHelper.getSaleResponseDto(sale);
    productPriceDiscountRequestDto =
        SaleTestHelper.createProductPriceDiscountRequest(product.getId(), 1000, 10);
    List<ProductPriceDiscountRequestDto> productPriceDiscountRequestDtoList = new ArrayList<>();
    productPriceDiscountRequestDtoList.add(productPriceDiscountRequestDto);
    saleRequestDto =
        SaleTestHelper.createSaleRequest(
            seller.getId(), buyer.getId(), productPriceDiscountRequestDtoList);
  }

  @Test
  void testMapRequestToEntity() {
    when(saleMapper.mapRequestToEntity(saleRequestDto, seller, buyer, List.of(product)))
        .thenReturn(sale);

    Sale sale = saleMapper.mapRequestToEntity(saleRequestDto, seller, buyer, List.of(product));

    assertNotNull(sale);
    Assertions.assertEquals(saleRequestDto.getSellerId(), sale.getSeller().getId());
    Assertions.assertEquals(saleRequestDto.getBuyerId(), sale.getBuyer().getId());
    Assertions.assertEquals(saleRequestDto.getProducts().size(), sale.getProducts().size());
  }

  @Test
  void testMapEntityToResponseDto() {
    when(saleMapper.mapEntityToResponseDto(sale)).thenReturn(saleResponseDto);

    SaleResponseDto saleResponseDto = saleMapper.mapEntityToResponseDto(sale);

    assertNotNull(saleResponseDto);
    Assertions.assertEquals(saleResponseDto.getSeller().getId(), sale.getSeller().getId());
    Assertions.assertEquals(saleResponseDto.getBuyer().getId(), sale.getBuyer().getId());
    Assertions.assertEquals(saleResponseDto.getProducts().size(), sale.getProducts().size());
  }
}
