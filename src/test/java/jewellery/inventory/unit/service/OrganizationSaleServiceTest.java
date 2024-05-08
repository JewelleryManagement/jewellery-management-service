package jewellery.inventory.unit.service;

import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductDiscountRequestDto;
import jewellery.inventory.dto.request.PurchasedResourceQuantityRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.OrganizationSaleResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.ResourceReturnResponseDto;
import jewellery.inventory.exception.not_found.ProductNotFoundException;
import jewellery.inventory.exception.not_found.ResourceInOrganizationNotFoundException;
import jewellery.inventory.exception.not_found.ResourceNotFoundInSaleException;
import jewellery.inventory.exception.not_found.SaleNotFoundException;
import jewellery.inventory.exception.organization.OrganizationNotOwnerException;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.ProductNotSoldException;
import jewellery.inventory.exception.sale.EmptySaleException;
import jewellery.inventory.helper.*;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.SaleRepository;
import jewellery.inventory.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationSaleServiceTest {
  @InjectMocks private OrganizationSaleService organizationSaleService;
  @Mock private SaleService saleService;
  @Mock private SaleMapper saleMapper;
  @Mock private OrganizationService organizationService;
  @Mock private UserService userService;
  @Mock private ResourceInOrganizationService resourceInOrganizationService;
  @Mock private SaleRepository saleRepository;
  @Mock private ProductService productService;

  private User user;
  private SaleRequestDto saleRequestDto;
  private Organization seller;
  private User buyer;
  private Product product;
  private Resource resource;
  private ProductDiscountRequestDto productDiscountRequestDto;
  private PurchasedResourceQuantityRequestDto purchasedResourceQuantityRequestDto;
  private Sale sale;
  private ProductPriceDiscount productPriceDiscount;
  private ProductReturnResponseDto productReturnResponseDto;
  private PurchasedResourceInUser purchasedResourceInUser;
  private ResourceInOrganization resourceInOrganization;

  @BeforeEach
  void SetUp() {
    user = UserTestHelper.createTestUserWithRandomId();
    seller = OrganizationTestHelper.getTestOrganization();
    buyer = UserTestHelper.createTestUserWithId();
    resource = ResourceTestHelper.getPearl();
    product = ProductTestHelper.getTestProduct(user, new Resource());
    productDiscountRequestDto =
        SaleTestHelper.createProductPriceDiscountRequest(product.getId(), BigDecimal.TEN);
    purchasedResourceQuantityRequestDto = SaleTestHelper.createPurchasedResourceRequestDto();
    saleRequestDto =
        SaleTestHelper.createSaleRequest(
            seller.getId(),
            buyer.getId(),
            List.of(productDiscountRequestDto),
            List.of(purchasedResourceQuantityRequestDto));
    productPriceDiscount = SaleTestHelper.createTestProductPriceDiscount(product, sale);
    purchasedResourceInUser = SaleTestHelper.createPurchasedResource(BigDecimal.TEN);
    sale =
        SaleTestHelper.createSaleInOrganization(
            seller, buyer, List.of(productPriceDiscount), List.of(purchasedResourceInUser));
    resourceInOrganization =
        OrganizationTestHelper.createTestResourceInOrganization(
            sale.getResources().get(0).getResource(), sale.getOrganizationSeller());
    productPriceDiscount.setSale(sale);
    productReturnResponseDto = SaleTestHelper.createProductReturnResponseDto(product, buyer);
  }

  @Test
  void testCreateSaleShouldThrowWhenNoProductAndResourceInRequest() {
    saleRequestDto.setProducts(null);
    saleRequestDto.setResources(null);
    assertThrows(
        EmptySaleException.class, () -> organizationSaleService.createSale(saleRequestDto));

    saleRequestDto.setProducts(new ArrayList<>());
    saleRequestDto.setResources(new ArrayList<>());
    assertThrows(
        EmptySaleException.class, () -> organizationSaleService.createSale(saleRequestDto));
  }

  @Test
  void testCreateSaleOfProductSuccessfully() {
    sale.setResources(new ArrayList<>());
    when(organizationService.getOrganization(seller.getId())).thenReturn(seller);
    when(userService.getUser(buyer.getId())).thenReturn(buyer);
    product.setOrganization(seller);
    saleRequestDto.setResources(null);
    when(productService.getProduct(product.getId())).thenReturn(product);
    when(saleMapper.mapSaleFromOrganization(
            saleRequestDto, seller, buyer, new ArrayList<>(), new ArrayList<>()))
        .thenReturn(sale);
    when(saleRepository.save(sale)).thenReturn(sale);
    OrganizationSaleResponseDto organizationSaleResponseDto =
        SaleTestHelper.getOrganizationSaleResponseDto(sale);
    when(saleMapper.mapToOrganizationSaleResponseDto(sale)).thenReturn(organizationSaleResponseDto);

    OrganizationSaleResponseDto actualSale = organizationSaleService.createSale(saleRequestDto);

    assertNotNull(actualSale);
    assertEquals(1, actualSale.getProducts().size());
    verify(saleMapper, times(1))
        .mapSaleFromOrganization(
            saleRequestDto, seller, buyer, new ArrayList<>(), new ArrayList<>());
    verify(saleMapper, times(1)).mapToOrganizationSaleResponseDto(sale);
    verify(saleRepository, times(1)).save(sale);
  }

  @Test
  void testCreateSaleOfResourceSuccessfully() {
    when(organizationService.getOrganization(seller.getId())).thenReturn(seller);
    when(userService.getUser(buyer.getId())).thenReturn(buyer);
    when(resourceInOrganizationService.getResourceInOrganization(
            sale.getOrganizationSeller(), sale.getResources().get(0).getResource()))
            .thenReturn(resourceInOrganization);

    saleRequestDto.setProducts(null);
    when(saleMapper.mapSaleFromOrganization(
            saleRequestDto, seller, buyer, new ArrayList<>(), new ArrayList<>()))
        .thenReturn(sale);
    when(saleRepository.save(sale)).thenReturn(sale);
    OrganizationSaleResponseDto organizationSaleResponseDto =
        SaleTestHelper.getOrganizationSaleResponseDto(sale);
    when(saleMapper.mapToOrganizationSaleResponseDto(sale)).thenReturn(organizationSaleResponseDto);

    OrganizationSaleResponseDto actualSale = organizationSaleService.createSale(saleRequestDto);

    assertNotNull(actualSale);
    verify(saleMapper, times(1))
        .mapSaleFromOrganization(
            saleRequestDto, seller, buyer, new ArrayList<>(), new ArrayList<>());
    verify(saleMapper, times(1)).mapToOrganizationSaleResponseDto(sale);
    verify(saleRepository, times(1)).save(sale);
  }

  @Test
  void testCreateSaleOfBothProductAndResourceSuccessfully() {
    when(organizationService.getOrganization(seller.getId())).thenReturn(seller);
    when(userService.getUser(buyer.getId())).thenReturn(buyer);
    product.setOrganization(seller);
    when(productService.getProduct(product.getId())).thenReturn(product);
    when(resourceInOrganizationService.getResourceInOrganization(
            sale.getOrganizationSeller(), sale.getResources().get(0).getResource()))
            .thenReturn(resourceInOrganization);
    when(saleMapper.mapSaleFromOrganization(
            saleRequestDto, seller, buyer, new ArrayList<>(), new ArrayList<>()))
        .thenReturn(sale);
    when(saleRepository.save(sale)).thenReturn(sale);
    OrganizationSaleResponseDto organizationSaleResponseDto =
        SaleTestHelper.getOrganizationSaleResponseDto(sale);
    when(saleMapper.mapToOrganizationSaleResponseDto(sale)).thenReturn(organizationSaleResponseDto);

    OrganizationSaleResponseDto actualSale = organizationSaleService.createSale(saleRequestDto);

    assertNotNull(actualSale);
    verify(saleMapper, times(1))
        .mapSaleFromOrganization(
            saleRequestDto, seller, buyer, new ArrayList<>(), new ArrayList<>());
    verify(saleMapper, times(1)).mapToOrganizationSaleResponseDto(sale);
    verify(saleRepository, times(1)).save(sale);
  }

  @Test
  void testCreateSaleShouldThrowWhenProductIsSold() {
    product.setPartOfSale(new ProductPriceDiscount());
    when(saleMapper.mapSaleFromOrganization(
            saleRequestDto, seller, buyer, new ArrayList<>(), new ArrayList<>()))
        .thenReturn(sale);
    when(organizationService.getOrganization(seller.getId())).thenReturn(seller);
    when(userService.getUser(buyer.getId())).thenReturn(buyer);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);

    assertThrows(
        ProductIsSoldException.class, () -> organizationSaleService.createSale(saleRequestDto));
  }

  @Test
  void testCreateSaleShouldThrowWhenProductIsPartOfProduct() {
    product.setContentOf(new Product());
    when(saleMapper.mapSaleFromOrganization(
            saleRequestDto, seller, buyer, new ArrayList<>(), new ArrayList<>()))
        .thenReturn(sale);
    when(organizationService.getOrganization(seller.getId())).thenReturn(seller);
    when(userService.getUser(buyer.getId())).thenReturn(buyer);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);

    assertThrows(
        ProductIsContentException.class, () -> organizationSaleService.createSale(saleRequestDto));
  }

  @Test
  void testCreateSaleShouldThrowWhenSellerNotOwner() {
    product.setOrganization(OrganizationTestHelper.getTestOrganization());
    when(saleMapper.mapSaleFromOrganization(
            saleRequestDto, seller, buyer, new ArrayList<>(), new ArrayList<>()))
        .thenReturn(sale);
    when(organizationService.getOrganization(seller.getId())).thenReturn(seller);
    when(userService.getUser(buyer.getId())).thenReturn(buyer);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);

    assertThrows(
        OrganizationNotOwnerException.class,
        () -> organizationSaleService.createSale(saleRequestDto));
  }

  @Test
  void testCreateSaleShouldThrowWhenResourceNotOwned() {
    product.setOrganization(seller);
    when(resourceInOrganizationService.findResourceInOrganizationOrThrow(
            seller, purchasedResourceQuantityRequestDto.getResourceAndQuantity().getResourceId()))
        .thenThrow(
            new ResourceInOrganizationNotFoundException(
                purchasedResourceQuantityRequestDto.getResourceAndQuantity().getResourceId(),
                seller.getId()));
    when(saleMapper.mapSaleFromOrganization(
            saleRequestDto, seller, buyer, new ArrayList<>(), new ArrayList<>()))
        .thenReturn(sale);
    when(organizationService.getOrganization(seller.getId())).thenReturn(seller);
    when(userService.getUser(buyer.getId())).thenReturn(buyer);
    when(productService.getProduct(product.getId())).thenReturn(product);

    assertThrows(
        ResourceInOrganizationNotFoundException.class,
        () -> organizationSaleService.createSale(saleRequestDto));
  }

  @Test
  void testReturnProductShouldThrowWhenProductNotFound() {
    when(productService.getProduct(product.getId()))
        .thenThrow(new ProductNotFoundException(product.getId()));

    assertThrows(
        ProductNotFoundException.class,
        () -> organizationSaleService.returnProduct(product.getId()));
  }

  @Test
  void testReturnProductShouldThrowWhenProductIsPartOfAnotherProduct() {
    when(productService.getProduct(product.getId())).thenReturn(product);
    product.setContentOf(new Product());

    assertThrows(
        ProductIsContentException.class,
        () -> organizationSaleService.returnProduct(product.getId()));
  }

  @Test
  void testReturnProductShouldThrowWhenProductIsSold() {
    when(productService.getProduct(product.getId())).thenReturn(product);
    product.setPartOfSale(null);

    assertThrows(
        ProductNotSoldException.class,
        () -> organizationSaleService.returnProduct(product.getId()));
  }

  @Test
  void testReturnProductSuccessfully() {
    product.setPartOfSale(productPriceDiscount);
    Product productBeforeReturn = product;

    assertEquals(1, sale.getProducts().size());
    assertNotNull(productBeforeReturn.getPartOfSale());

    sale.setResources(new ArrayList<>());
    when(productService.getProduct(product.getId())).thenReturn(product);
    when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));
    when(organizationSaleService.returnProduct(product.getId()))
        .thenReturn(productReturnResponseDto);

    ProductReturnResponseDto actual = organizationSaleService.returnProduct(product.getId());

    assertNull(actual.getSaleAfter());
    assertNotNull(actual.getReturnedProduct());
    assertNull(actual.getReturnedProduct().getPartOfSale());
    assertNotEquals(actual.getReturnedProduct().getOwner().getId(), product.getOwner().getId());
  }

  @Test
  void testReturnResourceShouldThrowWhenSaleNotFound() {
    assertThrows(
        SaleNotFoundException.class,
        () -> organizationSaleService.returnResource(sale.getId(), resource.getId()));
  }

  @Test
  void testReturnResourceShouldThrowWhenResourceNotFoundInSale() {
    when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));
    when(saleService.getPurchasedResource(
            purchasedResourceInUser.getResource().getId(), sale.getId()))
        .thenThrow(ResourceNotFoundInSaleException.class);

    assertThrows(
        ResourceNotFoundInSaleException.class,
        () -> organizationSaleService.returnResource(sale.getId(), resource.getId()));
  }

  @Test
  void testReturnResourceSuccessfully() {
    when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));
    when(saleService.getPurchasedResource(
            purchasedResourceInUser.getResource().getId(), sale.getId()))
        .thenReturn(purchasedResourceInUser);
    when(resourceInOrganizationService.getResourceInOrganization(
            sale.getOrganizationSeller(), resourceInOrganization.getResource()))
        .thenReturn(resourceInOrganization);

    assertEquals(BigDecimal.ONE, resourceInOrganization.getQuantity());
    assertEquals(getBigDecimal("5"), purchasedResourceInUser.getQuantity());
    assertEquals(1, sale.getResources().size());

    when(organizationSaleService.returnResource(
            sale.getId(), purchasedResourceInUser.getResource().getId()))
        .thenReturn(new ResourceReturnResponseDto());

    ResourceReturnResponseDto actual =
        organizationSaleService.returnResource(
            sale.getId(), purchasedResourceInUser.getResource().getId());

    assertNotNull(actual);
    assertEquals(0, sale.getResources().size());
    assertEquals(getBigDecimal("11"), resourceInOrganization.getQuantity());
  }

  @Test
  void testGetSaleShouldThrowWhenSaleNotFound() {
    assertThrows(SaleNotFoundException.class,
            () -> organizationSaleService.getSale(sale.getId()));
  }

  @Test
  void testGetSaleSuccessfully() {
    when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));

    organizationSaleService.getSale(sale.getId());

    verify(saleRepository, times(1)).findById(sale.getId());
  }
}
