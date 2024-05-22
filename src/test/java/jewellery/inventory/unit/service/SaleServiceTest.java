package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;
import jewellery.inventory.dto.request.ProductDiscountRequestDto;
import jewellery.inventory.dto.request.PurchasedResourceQuantityRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.PurchasedResourceQuantityResponseDto;
import jewellery.inventory.dto.response.ResourceReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.not_found.ResourceNotFoundInSaleException;
import jewellery.inventory.exception.not_found.SaleNotFoundException;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.ProductNotSoldException;
import jewellery.inventory.exception.product.UserNotOwnerException;
import jewellery.inventory.exception.sale.EmptySaleException;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.helper.SaleTestHelper;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.*;
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
  @Mock private ResourceService resourceService;
  @Mock private PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  @Mock private SaleMapper saleMapper;
  @Mock private ResourceInUserService resourceInUserService;
  private User seller;
  private User buyer;
  private Product product;
  private Product product2;
  private Sale sale;
  private SaleRequestDto saleRequestDto;
  private SaleRequestDto saleRequestDtoSellerNotOwner;
  private SaleResponseDto saleResponseDto;
  private ProductReturnResponseDto productReturnResponseDto;
  private ProductPriceDiscount productPriceDiscount;
  private PurchasedResourceInUser purchasedResourceInUser;
  private Resource resource;

  @BeforeEach
  void setUp() {
    seller = createTestUser();
    seller.setId(UUID.randomUUID());
    buyer = createSecondTestUser();
    product = getTestProduct(seller, new Resource());
    product2 = getTestProduct(seller, new Resource());
    resource = ResourceTestHelper.getPearl();
    resource.setPricePerQuantity(BigDecimal.TEN);
    purchasedResourceInUser = SaleTestHelper.createPurchasedResource(BigDecimal.TEN);
    productPriceDiscount = SaleTestHelper.createTestProductPriceDiscount(product, sale);
    sale =
        SaleTestHelper.createSaleWithTodayDate(
            seller, buyer, List.of(productPriceDiscount), List.of(purchasedResourceInUser));
    productPriceDiscount.setSale(sale);
    ProductDiscountRequestDto productDiscountRequestDto =
        SaleTestHelper.createProductPriceDiscountRequest(product.getId(), getBigDecimal("10"));
    PurchasedResourceQuantityRequestDto purchasedResourceQuantityRequestDto =
              SaleTestHelper.createPurchasedResourceRequestDto();
    saleRequestDto =
        SaleTestHelper.createSaleRequest(
            seller.getId(),
            buyer.getId(),
            List.of(productDiscountRequestDto),
            List.of(purchasedResourceQuantityRequestDto));
    saleRequestDtoSellerNotOwner =
        SaleTestHelper.createSaleRequest(
            buyer.getId(),
            buyer.getId(),
            List.of(productDiscountRequestDto),
            List.of(purchasedResourceQuantityRequestDto));
    saleResponseDto = SaleTestHelper.getSaleResponseDto(sale, BigDecimal.ONE, BigDecimal.TEN);
    productReturnResponseDto = SaleTestHelper.createProductReturnResponseDto(product, buyer);
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
    when(resourceService.getResourceById(any())).thenReturn(resource);
    when(saleMapper.mapRequestToEntity(
            any(SaleRequestDto.class), any(User.class), any(User.class), anyList(), anyList()))
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
    assertEquals(
        saleRequestDto.getResources().get(0).getDiscount(),
        actual.getResources().get(0).getDiscount());
    assertEquals(saleResponseDto.getTotalDiscount(), actual.getTotalDiscount());
  }

  @Test
  void testCreateSaleProductWillThrowsProductOwnerNotSeller() {
    when(resourceService.getResourceById(any())).thenReturn(resource);
    when(saleMapper.mapRequestToEntity(
            any(SaleRequestDto.class), any(User.class), any(User.class), anyList(), anyList()))
        .thenReturn(sale);
    when(userService.getUser(any(UUID.class))).thenReturn(seller, buyer);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);
    assertThrows(
        UserNotOwnerException.class, () -> saleService.createSale(saleRequestDtoSellerNotOwner));
  }

  @Test
  void testCreateSaleProductWillThrowsProductIsSold() {
    when(resourceService.getResourceById(any())).thenReturn(resource);
    product.setPartOfSale(new ProductPriceDiscount());
    when(saleMapper.mapRequestToEntity(
            any(SaleRequestDto.class), any(User.class), any(User.class), anyList(), anyList()))
        .thenReturn(sale);
    when(userService.getUser(any(UUID.class))).thenReturn(seller, buyer);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);

    assertThrows(ProductIsSoldException.class, () -> saleService.createSale(saleRequestDto));
  }

  @Test
  void testCreateSaleProductWillThrowsProductIsPartOfAnotherProduct() {
    when(resourceService.getResourceById(any())).thenReturn(resource);
    product.setContentOf(product2);

    when(saleMapper.mapRequestToEntity(
            any(SaleRequestDto.class), any(User.class), any(User.class), anyList(), anyList()))
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
    product.setPartOfSale(productPriceDiscount);
    UUID productId = product.getId();
    when(productService.getProduct(any(UUID.class))).thenReturn(product);
    assertThrows(SaleNotFoundException.class, () -> saleService.returnProduct(productId));
  }

  @Test
  void testReturnProductSuccessfully() {
    product.setPartOfSale(productPriceDiscount);
    Product productBeforeReturn = product;

    assertEquals(1, sale.getProducts().size());
    assertNotNull(productBeforeReturn.getPartOfSale());

    when(productService.getProduct(any(UUID.class))).thenReturn(product);
    when(saleRepository.findById(product.getPartOfSale().getSale().getId()))
        .thenReturn(Optional.of(sale));
    when(saleService.returnProduct(product.getId())).thenReturn(productReturnResponseDto);
    ProductReturnResponseDto result = saleService.returnProduct(product.getId());

    assertNull(result.getSaleAfter());
    assertNotNull(result.getReturnedProduct());
    assertNull(result.getReturnedProduct().getPartOfSale());
    assertNotEquals(
        result.getReturnedProduct().getOwner().getId(), productBeforeReturn.getOwner().getId());
  }

  @Test
  void testReturnResourceShouldThrowWhenSaleNotFound() {
    assertThrows(
        SaleNotFoundException.class,
        () ->
            saleService.returnResource(
                sale.getId(), purchasedResourceInUser.getResource().getId()));
  }

  @Test
  void testReturnResourceShouldThrowWhenResourceInNotPartOfThisSale() {
    when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));
    sale.setResources(new ArrayList<>());
    assertThrows(
        ResourceNotFoundInSaleException.class,
        () ->
            saleService.returnResource(
                sale.getId(), purchasedResourceInUser.getResource().getId()));
  }

  @Test
  void testReturnResourceSuccessfully() {
    when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));
    when(purchasedResourceInUserRepository.findByResourceIdAndPartOfSaleId(
            purchasedResourceInUser.getResource().getId(), sale.getId()))
        .thenReturn(Optional.of(purchasedResourceInUser));
    purchasedResourceInUser.setPartOfSale(sale);

    ResourceInUser resourceInUser = SaleTestHelper.createResourceInUser(BigDecimal.TEN);
    when(resourceInUserService.getResourceInUser(
            sale.getSeller(), purchasedResourceInUser.getResource()))
        .thenReturn(resourceInUser);

    assertEquals(BigDecimal.TEN, resourceInUser.getQuantity());
    assertEquals(getBigDecimal("5"), purchasedResourceInUser.getQuantity());
    assertEquals(1, sale.getResources().size());

    when(saleService.returnResource(sale.getId(), purchasedResourceInUser.getResource().getId()))
        .thenReturn(new ResourceReturnResponseDto());

    resourceInUser.setQuantity(BigDecimal.TEN);
    ResourceReturnResponseDto actualReturnResourceResponse =
        saleService.returnResource(sale.getId(), purchasedResourceInUser.getResource().getId());

    assertNotNull(actualReturnResourceResponse);
    assertEquals(0, sale.getResources().size());
    assertEquals(getBigDecimal("15"), resourceInUser.getQuantity());
  }

  @Test
  void testGetAllPurchasedResourcesSuccessfully() {

    User user = UserTestHelper.createTestUserWithId();
    PurchasedResourceQuantityResponseDto purchasedResourceQuantityResponseDto =
        SaleTestHelper.createPurchasedResourceResponseDto();

    when(resourceInUserService.getAllPurchasedResources(user.getId()))
        .thenReturn(List.of(purchasedResourceQuantityResponseDto));
    List<PurchasedResourceQuantityResponseDto> actualResponse =
        resourceInUserService.getAllPurchasedResources(user.getId());

    assertNotNull(actualResponse);
    assertEquals(1, actualResponse.size());

    assertEquals(
        purchasedResourceQuantityResponseDto.getResourceAndQuantity().getResource().getId(),
        actualResponse.get(0).getResourceAndQuantity().getResource().getId());
  }

  @Test
  void testCreateSaleShouldThrowWhenResourcesAndProductsInRequestAreNullOrEmpty() {
    saleRequestDto.setProducts(null);
    saleRequestDto.setResources(null);
    assertThrows(EmptySaleException.class, () -> saleService.createSale(saleRequestDto));

    saleRequestDto.setProducts(new ArrayList<>());
    saleRequestDto.setResources(new ArrayList<>());
    assertThrows(EmptySaleException.class, () -> saleService.createSale(saleRequestDto));
  }
}
