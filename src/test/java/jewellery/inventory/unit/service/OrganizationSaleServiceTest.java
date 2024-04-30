package jewellery.inventory.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductDiscountRequestDto;
import jewellery.inventory.dto.request.PurchasedResourceQuantityRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.OrganizationSaleResponseDto;
import jewellery.inventory.exception.not_found.ResourceInOrganizationNotFoundException;
import jewellery.inventory.exception.organization.OrganizationNotOwnerException;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
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

  @BeforeEach
  void SetUp() {
    user = UserTestHelper.createTestUserWithRandomId();
    seller = OrganizationTestHelper.getTestOrganization();
    buyer = UserTestHelper.createTestUserWithId();
    resource = ResourceTestHelper.getPearl();
    product = ProductTestHelper.getTestProduct(user, resource);
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
    sale =
        SaleTestHelper.createSaleInOrganization(seller, buyer, List.of(productPriceDiscount), null);
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
    when(resourceInOrganizationService
            .findResourceInOrganizationOrThrow(seller, purchasedResourceQuantityRequestDto.getResourceAndQuantity().getResourceId()))
            .thenThrow(new ResourceInOrganizationNotFoundException(purchasedResourceQuantityRequestDto.getResourceAndQuantity().getResourceId(), seller.getId()));
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
}
