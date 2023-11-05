package jewellery.inventory.integration;

import static jewellery.inventory.helper.ProductTestHelper.getTestProduct;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createSecondTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import jewellery.inventory.dto.request.ProductPriceDiscountRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.helper.SaleTestHelper;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

class SaleIntegrationTest extends AuthenticatedIntegrationTestBase {
  private String getBaseUrl() {
    return BASE_URL_PATH + port;
  }

  private String getBaseSaleUrl() {
    return getBaseUrl() + "/sales";
  }

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
  void createSaleSuccessfully() {
    UserRequestDto seller= UserTestHelper.createTestUserRequest();
    UserRequestDto buyer= UserTestHelper.createDifferentUserRequest();

    ResponseEntity<SaleResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseSaleUrl(), saleRequestDto, SaleResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  void getAllSalesSuccessfully() {

    ResponseEntity<List<SaleResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseSaleUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
    assertNotNull(response.getBody());
  }
}
