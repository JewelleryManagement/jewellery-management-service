package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.*;
import jewellery.inventory.dto.request.ProductPriceDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
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
  @Mock private ProductRepository productRepository;
  @Mock private SaleMapper saleMapper;
  private User seller;
  private User buyer;
  private Product product;
  private Sale sale;
  private Sale saleTwoProducts;
  private ProductReturnResponseDto productReturnResponseDto;
  private SaleRequestDto saleRequestDto;
  private SaleRequestDto saleRequestDtoSellerNotOwner;
  private SaleResponseDto saleResponseDto;
  private ProductPriceDiscountRequestDto productPriceDiscountRequestDto;
  private List<Product> productsForSale;
  private List<Product> productsForSaleTwo;
  private ProductResponseDto productResponseDto;

  @BeforeEach
  void setUp() {
    seller = createTestUser();
    seller.setId(UUID.randomUUID());
    buyer = createSecondTestUser();
    product = getTestProduct(seller, new Resource());
    productsForSale = SaleTestHelper.getProductsList(product);
    productsForSaleTwo = SaleTestHelper.getProductsList(product, product);
    sale = SaleTestHelper.createSaleWithTodayDate(seller, buyer, productsForSale);
    saleTwoProducts = SaleTestHelper.createSaleWithTodayDate(seller, buyer, productsForSaleTwo);
    saleResponseDto = SaleTestHelper.getSaleResponseDto(sale);
    productResponseDto = getReturnedProductResponseDto(product, createTestUserResponseDto(buyer));
    productReturnResponseDto =
        SaleTestHelper.getProductReturnResponseDto(saleResponseDto, productResponseDto);
    productPriceDiscountRequestDto =
        SaleTestHelper.createProductPriceDiscountRequest(
            product.getId(), getBigDecimal("10"));
    List<ProductPriceDiscountRequestDto> productPriceDiscountRequestDtoList = new ArrayList<>();
    productPriceDiscountRequestDtoList.add(productPriceDiscountRequestDto);
    saleRequestDto =
        SaleTestHelper.createSaleRequest(
            seller.getId(), buyer.getId(), productPriceDiscountRequestDtoList);
    saleRequestDtoSellerNotOwner =
        SaleTestHelper.createSaleRequest(
            buyer.getId(), buyer.getId(), productPriceDiscountRequestDtoList);
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
//    when(saleMapper.mapRequestToEntity(saleRequestDto, seller, buyer, List.of(product)))
//        .thenReturn(sale);
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
//    when(saleMapper.mapRequestToEntity(
//            saleRequestDtoSellerNotOwner, seller, buyer, List.of(product)))
//        .thenReturn(sale);
    when(userService.getUser(any(UUID.class))).thenReturn(seller, buyer);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);

    assertThrows(
        UserNotOwnerException.class, () -> saleService.createSale(saleRequestDtoSellerNotOwner));
  }

//  @Test
//  void testCreateSaleProductWillThrowsProductIsSold() {
//    when(saleMapper.mapRequestToEntity(saleRequestDto, seller, buyer, List.of(product)))
//        .thenReturn(sale);
////    sale.getProducts().get(0).setPartOfSale(new Sale());
//    when(userService.getUser(any(UUID.class))).thenReturn(seller, buyer);
//    when(productService.getProduct(any(UUID.class))).thenReturn(product);
//
//    assertThrows(ProductIsSoldException.class, () -> saleService.createSale(saleRequestDto));
//  }

  @Test
  void testCreateSaleProductWillThrowsProductIsPartOfAnotherProduct() {
//    when(saleMapper.mapRequestToEntity(saleRequestDto, seller, buyer, List.of(product)))
//        .thenReturn(sale);
////    sale.getProducts().get(0).setContentOf(new Product());
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
    product.setPartOfSale(new Sale());
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
  void testReturnProductSuccessfullyWithTwoProducts() {
    product.setPartOfSale(saleTwoProducts);
    Product productBeforeReturn = product;

    assertEquals(2, saleTwoProducts.getProducts().size());
    assertNotNull(productBeforeReturn.getPartOfSale());

    when(productService.getProduct(any(UUID.class))).thenReturn(product);
    when(saleRepository.findById(any(UUID.class))).thenReturn(Optional.of(saleTwoProducts));
    when(saleService.returnProduct(product.getId())).thenReturn(productReturnResponseDto);

    ProductReturnResponseDto productReturnResponseDto = saleService.returnProduct(product.getId());

    assertEquals(1, productReturnResponseDto.getSaleAfter().getProducts().size());
    assertNull(productReturnResponseDto.getSaleAfter().getId());
    assertNotNull(productReturnResponseDto.getReturnedProduct());
    assertNull(productReturnResponseDto.getReturnedProduct().getPartOfSale());
    assertNotEquals(
        productReturnResponseDto.getReturnedProduct().getOwner().getId(),
        productBeforeReturn.getOwner().getId());
  }
}
