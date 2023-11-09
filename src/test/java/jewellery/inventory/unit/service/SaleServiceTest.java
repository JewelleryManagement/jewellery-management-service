package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.*;
import jewellery.inventory.dto.request.ProductPriceDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.product.ProductOwnerNotSeller;
import jewellery.inventory.helper.SaleTestHelper;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.mapper.UserMapper;
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
  @Mock private UserMapper userMapper;
  @Mock private UserService userService;
  @Mock private ProductService productService;
  @Mock private ProductRepository productRepository;
  @Mock private SaleMapper saleMapper;
  private User seller;
  private User buyer;
  private Product product;
  private Sale sale;
  private SaleRequestDto saleRequestDto;
  private SaleRequestDto saleRequestDtoOwnerEqualsRecipient;
  private SaleRequestDto saleRequestDtoSellerNotOwner;
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
    saleRequestDtoOwnerEqualsRecipient =
        SaleTestHelper.createSaleRequest(
            seller.getId(), seller.getId(), productPriceDiscountRequestDtoList);
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
  void testCreateSaleProductWillSuccessfully() {
    when(saleMapper.mapRequestToEntity(saleRequestDto, seller, buyer, List.of(product)))
        .thenReturn(sale);
    when(userMapper.toUserEntity(userService.getUser(any(UUID.class)))).thenReturn(seller, buyer);
    when(saleRepository.save(sale)).thenReturn(sale);
    when(productService.returnProductByID(any(UUID.class))).thenReturn(product);

    when(saleMapper.mapEntityToResponseDto(sale)).thenReturn(saleResponseDto);

    SaleResponseDto actual = saleService.createSale(saleRequestDto);
    assertNotEquals(actual.getBuyer(), actual.getSeller());
    Assertions.assertEquals(saleRequestDto.getSellerId(), actual.getSeller().getId());
    assertNotNull(actual);
  }

  @Test
  void testCreateSaleProductWillThrowsProductOwnerNotSeller() {
    when(saleMapper.mapRequestToEntity(
            saleRequestDtoSellerNotOwner, seller, buyer, List.of(product)))
        .thenReturn(sale);
    when(userMapper.toUserEntity(userService.getUser(any(UUID.class)))).thenReturn(seller, buyer);
    when(productService.returnProductByID(any(UUID.class))).thenReturn(product);

    assertThrows(
        ProductOwnerNotSeller.class, () -> saleService.createSale(saleRequestDtoSellerNotOwner));
  }
}