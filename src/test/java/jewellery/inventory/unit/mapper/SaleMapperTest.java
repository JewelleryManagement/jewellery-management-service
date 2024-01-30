package jewellery.inventory.unit.mapper;

import static jewellery.inventory.helper.ProductTestHelper.getTestProduct;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jewellery.inventory.dto.request.ProductDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.helper.ProductPriceDiscountTestHelper;
import jewellery.inventory.helper.SaleTestHelper;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaleMapperTest {
  @InjectMocks private SaleMapper saleMapper;
  @Mock private UserMapper userMapper;
  @Mock private ProductMapper productMapper;
  private User seller;
  private User buyer;
  private UserResponseDto sellerResponseDto;
  private UserResponseDto buyerResponseDto;
  private Product product;
  private Sale sale;
  private SaleRequestDto saleRequestDto;
  private ProductDiscountRequestDto productDiscountRequestDto;
  private List<Product> productsForSale;
  private ProductPriceDiscount productPriceDiscount;

  @BeforeEach
  void setUp() {
    seller = createTestUser();
    buyer = createSecondTestUser();
    product = getTestProduct(seller, new Resource());
    sellerResponseDto = createTestUserResponseDto(seller);
    buyerResponseDto = createTestUserResponseDto(buyer);
    productsForSale = SaleTestHelper.getProductsList(product);
    sale = SaleTestHelper.createSaleWithTodayDate(seller, buyer);
    productDiscountRequestDto =
        SaleTestHelper.createProductPriceDiscountRequest(
            product.getId(),getBigDecimal("1000"));
    productPriceDiscount =
        ProductPriceDiscountTestHelper.createTestProductPriceDiscount(product, sale);
    List<ProductDiscountRequestDto> productDiscountRequestDtoList = new ArrayList<>();
    productDiscountRequestDtoList.add(productDiscountRequestDto);
    saleRequestDto =
        SaleTestHelper.createSaleRequest(
            seller.getId(), buyer.getId(), productDiscountRequestDtoList);
  }

  @Test
  void testMapRequestToEntity() {
    Sale sale =
        saleMapper.mapRequestToEntity(saleRequestDto, seller, buyer, List.of(productPriceDiscount));

    assertNotNull(sale);
    Assertions.assertEquals(saleRequestDto.getSellerId(), sale.getSeller().getId());
    Assertions.assertEquals(saleRequestDto.getBuyerId(), sale.getBuyer().getId());
    Assertions.assertEquals(saleRequestDto.getProducts().size(), sale.getProducts().size());
  }

  @Test
  void testMapEntityToResponseDto() {
    Sale sale =
            saleMapper.mapRequestToEntity(saleRequestDto, seller, buyer, List.of(productPriceDiscount));
    when(userMapper.toUserResponse(seller)).thenReturn(sellerResponseDto);
    when(userMapper.toUserResponse(buyer)).thenReturn(buyerResponseDto);

    SaleResponseDto saleResponseDto = saleMapper.mapEntityToResponseDto(sale);

    assertNotNull(saleResponseDto);
    Assertions.assertEquals(saleResponseDto.getSeller().getId(), sale.getSeller().getId());
    Assertions.assertEquals(saleResponseDto.getBuyer().getId(), sale.getBuyer().getId());
    Assertions.assertEquals(saleResponseDto.getProducts().size(), sale.getProducts().size());
    assertEquals(saleResponseDto.getDate(), sale.getDate());
  }

  @Test
  void testMapEntityToResponseDtoWillThrowsArithmeticException() {
    Sale sale =
            saleMapper.mapRequestToEntity(saleRequestDto, seller, buyer, List.of(productPriceDiscount));

    when(userMapper.toUserResponse(seller)).thenReturn(sellerResponseDto);
    when(userMapper.toUserResponse(buyer)).thenReturn(buyerResponseDto);

    when(productMapper.mapToProductResponseDto(product)).thenReturn(new ProductResponseDto());
    sale.getProducts().get(0).setSalePrice(BigDecimal.ZERO);
    assertThrows(ArithmeticException.class, () -> saleMapper.mapEntityToResponseDto(sale));
  }
}
