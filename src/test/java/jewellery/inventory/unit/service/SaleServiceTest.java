package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.util.*;
import jewellery.inventory.dto.request.ProductDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.not_found.SaleNotFoundException;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.ProductNotSoldException;
import jewellery.inventory.exception.product.UserNotOwnerException;
import jewellery.inventory.helper.SaleTestHelper;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.ProductService;
import jewellery.inventory.service.SaleService;
import jewellery.inventory.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {
  @InjectMocks private SaleService saleService;
  @Mock private SaleRepository saleRepository;
  @Mock private UserService userService;
  @Mock private ProductService productService;
  @Mock private SaleMapper saleMapper;
  private User seller;
  private User buyer;
  private Product product;
  private Sale sale;
  private SaleRequestDto saleRequestDto;
  private SaleRequestDto saleRequestDtoSellerNotOwner;
  private SaleResponseDto saleResponseDto;

  @BeforeEach
  void setUp() {
    seller = createTestUser();
    seller.setId(UUID.randomUUID());
    buyer = createSecondTestUser();
    product = getTestProduct(seller, new Resource());
    ProductPriceDiscount productPriceDiscount1 = SaleTestHelper.createTestProductPriceDiscount(product, sale);
    sale = SaleTestHelper.createSaleWithTodayDate(seller, buyer, List.of(productPriceDiscount1));
    ProductDiscountRequestDto productDiscountRequestDto =
        SaleTestHelper.createProductPriceDiscountRequest(product.getId(), getBigDecimal("10"));
    saleRequestDto =
        SaleTestHelper.createSaleRequest(
            seller.getId(), buyer.getId(), List.of(productDiscountRequestDto));
    saleRequestDtoSellerNotOwner =
        SaleTestHelper.createSaleRequest(
            buyer.getId(), buyer.getId(), List.of(productDiscountRequestDto));
    ProductPriceDiscount productPriceDiscount =
        SaleTestHelper.createTestProductPriceDiscount(product, sale);
    saleResponseDto = SaleTestHelper.getSaleResponseDto(sale, productPriceDiscount);
  }

  @Test
  void testGetAllSales() {

    List<Sale> sales = Arrays.asList(sale, new Sale(), new Sale());

    when(saleRepository.findAll()).thenReturn(sales);

    List<SaleResponseDto> responses = saleService.getAllSales();

    Assertions.assertEquals(sales.size(), responses.size());
  }

  @Test
  void testCreateSaleSuccessfully() {
    when(saleMapper.mapRequestToEntity(
            any(SaleRequestDto.class), any(User.class), any(User.class), anyList()))
        .thenReturn(sale);

    when(userService.getUser(any(UUID.class))).thenReturn(seller, buyer);
    when(saleRepository.save(sale)).thenReturn(sale);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);

    when(saleMapper.mapEntityToResponseDto(sale)).thenReturn(saleResponseDto);

    SaleResponseDto actual = saleService.createSale(saleRequestDto);

    assertNotNull(actual);
    assertEquals(1, actual.getProducts().size());
    assertEquals(saleRequestDto.getBuyerId(), actual.getProducts().get(0).getOwner().getId());
    assertEquals(saleRequestDto.getSellerId(), actual.getSeller().getId());
    assertNotEquals(actual.getBuyer(), actual.getSeller());
  }

  @Test
  void testCreateSaleProductWillThrowsProductOwnerNotSeller() {
    when(saleMapper.mapRequestToEntity(
            any(SaleRequestDto.class), any(User.class), any(User.class), anyList()))
        .thenReturn(sale);
    when(userService.getUser(any(UUID.class))).thenReturn(seller, buyer);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);
    assertThrows(
        UserNotOwnerException.class, () -> saleService.createSale(saleRequestDtoSellerNotOwner));
  }

  @Test
  void testCreateSaleProductWillThrowsProductIsSold() {
    product.setPartOfSale(sale);
    when(saleMapper.mapRequestToEntity(
            any(SaleRequestDto.class), any(User.class), any(User.class), anyList()))
        .thenReturn(sale);
    when(userService.getUser(any(UUID.class))).thenReturn(seller, buyer);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);

    assertThrows(ProductIsSoldException.class, () -> saleService.createSale(saleRequestDto));
  }

  @Test
  void testCreateSaleProductWillThrowsProductIsPartOfAnotherProduct() {
    product.setContentOf(product);
    when(saleMapper.mapRequestToEntity(
            any(SaleRequestDto.class), any(User.class), any(User.class), anyList()))
        .thenReturn(sale);
    when(userService.getUser(any(UUID.class))).thenReturn(seller, buyer);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);

    assertThrows(ProductIsContentException.class, () -> saleService.createSale(saleRequestDto));
  }

  @Test
  void testReturnProductWillThrowsProductNotSoldException() {
    UUID productId = product.getId();
    when(productService.getProduct(any(UUID.class))).thenReturn(product);
    assertThrows(ProductNotSoldException.class, () -> saleService.returnProduct(productId));
  }

  @Test
  void testReturnProductWillThrowsProductIsContentException() {
    product.setContentOf(new Product());
    UUID productId = product.getId();
    when(productService.getProduct(any(UUID.class))).thenReturn(product);
    assertThrows(ProductIsContentException.class, () -> saleService.returnProduct(productId));
  }

  @Test
  void testReturnProductWillThrowsSaleNotFoundException() {
    product.setPartOfSale(new Sale());
    UUID productId = product.getId();
    when(productService.getProduct(any(UUID.class))).thenReturn(product);
    assertThrows(SaleNotFoundException.class, () -> saleService.returnProduct(productId));
  }

  @Test
  void testReturnProductSuccessfully() {
    product.setPartOfSale(sale);
    Product productBeforeReturn = product;

    assertEquals(1, sale.getProducts().size());
    assertNotNull(productBeforeReturn.getPartOfSale());

    when(productService.getProduct(any(UUID.class))).thenReturn(product);
    when(saleRepository.findById(any(UUID.class))).thenReturn(Optional.of(sale));

    ProductReturnResponseDto result = saleService.returnProduct(product.getId());

    assertNull(result.getSaleAfter());
    assertNotNull(result.getReturnedProduct());
    assertNull(result.getReturnedProduct().getPartOfSale());
    assertNotEquals(
        result.getReturnedProduct().getOwner().getId(), productBeforeReturn.getOwner().getId());
  }
}
