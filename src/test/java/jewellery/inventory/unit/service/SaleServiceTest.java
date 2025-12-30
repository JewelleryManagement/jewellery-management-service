package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {
  @InjectMocks private SaleService saleService;
  @Mock private UserService userService;
  @Mock private ProductService productService;
  @Mock private ResourceService resourceService;
  @Mock private PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  @Mock private SaleMapper saleMapper;
  @Mock private ResourceInUserService resourceInUserService;
  private User seller;
  private User buyer;
  private Product product;
  private Sale sale;
  private ProductPriceDiscount productPriceDiscount;
  private PurchasedResourceInUser purchasedResourceInUser;
  private Resource resource;

  @BeforeEach
  void setUp() {
    seller = createTestUser();
    seller.setId(UUID.randomUUID());
    buyer = createSecondTestUser();
    product = getTestProduct(seller, new Resource());
    resource = ResourceTestHelper.getPearl();
    resource.setPricePerQuantity(BigDecimal.TEN);
    purchasedResourceInUser = SaleTestHelper.createPurchasedResource(BigDecimal.TEN);
    productPriceDiscount = SaleTestHelper.createTestProductPriceDiscount(product, sale);
    sale =
        SaleTestHelper.createSaleWithTodayDate(
            seller, buyer, List.of(productPriceDiscount), List.of(purchasedResourceInUser));
    productPriceDiscount.setSale(sale);
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
}
