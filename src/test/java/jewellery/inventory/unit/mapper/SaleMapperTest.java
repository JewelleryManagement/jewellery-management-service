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
import jewellery.inventory.dto.request.PurchasedResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.helper.SaleTestHelper;
import jewellery.inventory.mapper.*;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.PurchasedResourceInUser;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceRepository;
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
  @Mock private ResourceMapper resourceMapper;
  @Mock private ProductMapper productMapper;
  @Mock private ResourceRepository resourceRepository;
  @Mock private PurchasedResourceInUserMapper purchasedResourceInUserMapper;
  private User seller;
  private User buyer;
  private UserResponseDto sellerResponseDto;
  private UserResponseDto buyerResponseDto;
  private Product product;
  private Sale sale;
  private SaleRequestDto saleRequestDto;
  private ProductPriceDiscount productPriceDiscount;
  private PurchasedResourceInUser purchasedResourceInUser;
  private PurchasedResourceQuantityRequestDto purchasedResourceQuantityRequestDto;

  @BeforeEach
  void setUp() {
    seller = createTestUser();
    buyer = createSecondTestUser();
    product = getTestProduct(seller, new Resource());
    sellerResponseDto = createTestUserResponseDto(seller);
    buyerResponseDto = createTestUserResponseDto(buyer);
    purchasedResourceInUser = SaleTestHelper.createPurchasedResource(getBigDecimal("10"));
    productPriceDiscount = SaleTestHelper.createTestProductPriceDiscount(product, sale);
    sale =
        SaleTestHelper.createSaleWithTodayDate(
            seller, buyer, List.of(productPriceDiscount), List.of(purchasedResourceInUser));
    ProductDiscountRequestDto productDiscountRequestDto =
        SaleTestHelper.createProductPriceDiscountRequest(product.getId(), getBigDecimal("1000"));
    List<ProductDiscountRequestDto> productDiscountRequestDtoList = new ArrayList<>();
    productDiscountRequestDtoList.add(productDiscountRequestDto);
    purchasedResourceQuantityRequestDto = SaleTestHelper.createPurchasedResourceRequestDto();
    saleRequestDto =
        SaleTestHelper.createSaleRequest(
            seller.getId(),
            buyer.getId(),
            productDiscountRequestDtoList,
            List.of(purchasedResourceQuantityRequestDto));
    SaleTestHelper.createPurchasedResourceResponseDto();
  }

  @Test
  void testMapRequestToEntity() {
    Sale actual =
        saleMapper.mapRequestToEntity(
            saleRequestDto,
            seller,
            buyer,
            List.of(productPriceDiscount),
            List.of(purchasedResourceInUser));

    assertNotNull(actual);
    Assertions.assertEquals(saleRequestDto.getSellerId(), actual.getSeller().getId());
    Assertions.assertEquals(saleRequestDto.getBuyerId(), actual.getBuyer().getId());
    Assertions.assertEquals(saleRequestDto.getProducts().size(), actual.getProducts().size());
    Assertions.assertEquals(saleRequestDto.getResources().size(), actual.getResources().size());
  }

  @Test
  void testMapEntityToResponseDto() {
    Sale sale =
        saleMapper.mapRequestToEntity(
            saleRequestDto,
            seller,
            buyer,
            List.of(productPriceDiscount),
            List.of(purchasedResourceInUser));
    when(userMapper.toUserResponse(seller)).thenReturn(sellerResponseDto);
    when(userMapper.toUserResponse(buyer)).thenReturn(buyerResponseDto);
    when(productMapper.mapToProductResponseDto(product)).thenReturn(new ProductResponseDto());

    SaleResponseDto saleResponseDto = saleMapper.mapEntityToResponseDto(sale);

    assertNotNull(saleResponseDto);
    Assertions.assertEquals(saleResponseDto.getSeller().getId(), sale.getSeller().getId());
    Assertions.assertEquals(saleResponseDto.getBuyer().getId(), sale.getBuyer().getId());
    Assertions.assertEquals(saleResponseDto.getProducts().size(), sale.getProducts().size());
    Assertions.assertEquals(
        saleResponseDto.getResources().getResources().size(), sale.getResources().size());
    assertEquals(saleResponseDto.getDate(), sale.getDate());
  }

  @Test
  void testMappingWithZeroSalePriceThrowsArithmeticException() {
    Sale sale =
        saleMapper.mapRequestToEntity(
            saleRequestDto,
            seller,
            buyer,
            List.of(productPriceDiscount),
            List.of(purchasedResourceInUser));

    when(userMapper.toUserResponse(seller)).thenReturn(sellerResponseDto);
    when(userMapper.toUserResponse(buyer)).thenReturn(buyerResponseDto);

    when(productMapper.mapToProductResponseDto(product)).thenReturn(new ProductResponseDto());
    sale.getProducts().get(0).setSalePrice(BigDecimal.ZERO);
    sale.getResources().get(0).setSalePrice(BigDecimal.ZERO);
    assertThrows(ArithmeticException.class, () -> saleMapper.mapEntityToResponseDto(sale));
  }
}
