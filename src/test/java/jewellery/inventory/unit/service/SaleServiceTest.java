package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
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
import jewellery.inventory.repository.*;
import jewellery.inventory.service.SaleService;
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
  @Mock private ProductRepository productRepository;
  @Mock private UserRepository userRepository;
  @Mock private SaleRepository saleRepository;
  @Mock private SaleMapper saleMapper;
  private User seller;
  private User buyer;
  private Product product;
  private Sale sale;
  private SaleRequestDto saleRequestDto;
  private SaleResponseDto saleResponseDto;
  private ProductPriceDiscountRequestDto productPriceDiscountRequestDto;
  private List<SaleResponseDto> saleResponseDtoList;
  private List<Product> productsForSale;

  @BeforeEach
  void setUp() {
    seller = createTestUserForSale();
    buyer = createSecondTestUser();
    product = getTestProduct(seller, new Resource());
    productsForSale = SaleTestHelper.getProList(product);
    sale = SaleTestHelper.createSaleWithTodayDate(seller, buyer, productsForSale);
    saleResponseDto = SaleTestHelper.getSaleResponseDto(sale);
    productPriceDiscountRequestDto =
        SaleTestHelper.createProductPriceDiscountRequest(product.getId(), 1000, 10);
    List<ProductPriceDiscountRequestDto> productPriceDiscountRequestDtoList = new ArrayList<>();
    productPriceDiscountRequestDtoList.add(productPriceDiscountRequestDto);
    saleRequestDto =
        SaleTestHelper.createSaleRequest(
            seller.getId(), buyer.getId(), productPriceDiscountRequestDtoList);
    saleResponseDtoList = SaleTestHelper.getSaleResponseList(saleResponseDto);
  }

  @Test
  void testGetAllSales() {

    List<Sale> sales = Arrays.asList(sale, new Sale(), new Sale());

    when(saleRepository.findAll()).thenReturn(sales);

    List<SaleResponseDto> responses = saleService.getAllSales();

    Assertions.assertEquals(sales.size(), responses.size());
  }

  @Test
  void testCreateProductShouldSetContentProduct() {
    when(saleMapper.mapRequestToEntity(saleRequestDto)).thenReturn(sale);

    when(saleRepository.save(sale)).thenReturn(sale);

    SaleResponseDto responseDto = new SaleResponseDto();
    when(saleMapper.mapEntityToResponseDto(sale)).thenReturn(responseDto);

    SaleResponseDto actual = saleService.createSale(saleRequestDto);

    Assertions.assertNotNull(actual);
  }
}
