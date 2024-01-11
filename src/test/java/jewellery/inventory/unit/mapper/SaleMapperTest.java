package jewellery.inventory.unit.mapper;

import static jewellery.inventory.helper.ProductTestHelper.getTestProduct;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jewellery.inventory.dto.request.ProductPriceDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.helper.SaleTestHelper;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.utils.BigDecimalUtil;
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
  private ProductPriceDiscountRequestDto productPriceDiscountRequestDto;
  private List<Product> productsForSale;

  @BeforeEach
  void setUp() {
    seller = createTestUser();
    buyer = createSecondTestUser();
    product = getTestProduct(seller, new Resource());
    sellerResponseDto = createTestUserResponseDto(seller);
    buyerResponseDto = createTestUserResponseDto(buyer);
    productsForSale = SaleTestHelper.getProductsList(product);
    sale = SaleTestHelper.createSaleWithTodayDate(seller, buyer, productsForSale);
    productPriceDiscountRequestDto =
        SaleTestHelper.createProductPriceDiscountRequest(
            product.getId(),
            BigDecimalUtil.getBigDecimal("1000"),
            BigDecimalUtil.getBigDecimal("10"));
    List<ProductPriceDiscountRequestDto> productPriceDiscountRequestDtoList = new ArrayList<>();
    productPriceDiscountRequestDtoList.add(productPriceDiscountRequestDto);
    saleRequestDto =
        SaleTestHelper.createSaleRequest(
            seller.getId(), buyer.getId(), productPriceDiscountRequestDtoList);
  }

  @Test
  void testMapRequestToEntity() {
    Sale sale = saleMapper.mapRequestToEntity(saleRequestDto, seller, buyer, List.of(product));

    assertNotNull(sale);
    Assertions.assertEquals(saleRequestDto.getSellerId(), sale.getSeller().getId());
    Assertions.assertEquals(saleRequestDto.getBuyerId(), sale.getBuyer().getId());
    Assertions.assertEquals(saleRequestDto.getProducts().size(), sale.getProducts().size());
  }

  @Test
  void testMapEntityToResponseDto() {
    when(userMapper.toUserResponse(seller)).thenReturn(sellerResponseDto);
    when(userMapper.toUserResponse(buyer)).thenReturn(buyerResponseDto);

    when(productMapper.mapToProductResponseDto(product)).thenReturn(new ProductResponseDto());

    SaleResponseDto saleResponseDto = saleMapper.mapEntityToResponseDto(sale);

    assertNotNull(saleResponseDto);
    Assertions.assertEquals(saleResponseDto.getSeller().getId(), sale.getSeller().getId());
    Assertions.assertEquals(saleResponseDto.getBuyer().getId(), sale.getBuyer().getId());
    Assertions.assertEquals(saleResponseDto.getProducts().size(), sale.getProducts().size());
    assertEquals(saleResponseDto.getDate(), sale.getDate());
  }

  @Test
  void testMapEntityToResponseDtoWillThrowsIllegalArgumentException() {
    when(userMapper.toUserResponse(seller)).thenReturn(sellerResponseDto);
    when(userMapper.toUserResponse(buyer)).thenReturn(buyerResponseDto);

    when(productMapper.mapToProductResponseDto(product)).thenReturn(new ProductResponseDto());
    sale.getProducts().get(0).setSalePrice(BigDecimal.ZERO);
    assertThrows(IllegalArgumentException.class, () -> saleMapper.mapEntityToResponseDto(sale));
  }
}
