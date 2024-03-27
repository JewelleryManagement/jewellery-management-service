package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.ProductTestHelper.getTestProduct;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductsInOrganizationResponseDto;
import jewellery.inventory.helper.*;
import jewellery.inventory.mapper.ProductInOrganizationMapper;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInOrganization;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.repository.ResourceInProductRepository;
import jewellery.inventory.service.OrganizationService;
import jewellery.inventory.service.ProductInOrganizationService;
import jewellery.inventory.service.ProductService;
import jewellery.inventory.service.ResourceInOrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductInOrganizationServiceTest {
  @InjectMocks private ProductInOrganizationService productInOrganizationService;
  @Mock private OrganizationService organizationService;
  @Mock private ProductService productService;
  @Mock private ProductInOrganizationMapper mapper;
  @Mock private ResourceInOrganizationService resourceInOrganizationService;
  @Mock private ResourceInProductRepository resourceInProductRepository;
  @Mock private ProductRepository productRepository;
  @Mock private ProductMapper productMapper;
  private Organization organization;
  private Organization organizationWithProduct;
  private OrganizationResponseDto organizationResponseDto;

  private Resource resource;
  private ResourceInOrganizationRequestDto resourceInOrganizationRequestDto;
  private ResourceInOrganization resourceInOrganization;
  private ProductRequestDto productRequestDto;
  private User user;
  private Product product;
  private ProductResponseDto productResponseDto;
  private static final BigDecimal QUANTITY = BigDecimal.ONE;
  private static final BigDecimal NEGATIVE_QUANTITY = BigDecimal.valueOf(-5);
  private static final BigDecimal BIG_QUANTITY = BigDecimal.valueOf(30);
  private static final BigDecimal DEAL_PRICE = BigDecimal.TEN;
  private ProductsInOrganizationResponseDto productsInOrganizationResponseDto;

  @BeforeEach
  void setUp() {
    user = UserTestHelper.createSecondTestUser();
    organization = OrganizationTestHelper.getTestOrganization();
    resource = ResourceTestHelper.getPearl();
    resourceInOrganizationRequestDto =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resource.getId(), QUANTITY, DEAL_PRICE);
    resourceInOrganization =
        ResourceInOrganizationTestHelper.createResourceInOrganization(organization, resource);
    organization.setResourceInOrganization(List.of(resourceInOrganization));
    productRequestDto = ProductTestHelper.getBaseProductRequestDtoForOrganization(user);
    productRequestDto =
        setOwnerAndResourceToProductRequest(
            productRequestDto, organization.getId(), user.getId(), BIG_QUANTITY);
    product = getTestProduct(user, resource);
    organizationWithProduct =
        setProductAndRsourcesToOrganization(organization, product, resourceInOrganization);
    productResponseDto = productToResponse(product);
    organizationResponseDto = getTestOrganizationResponseDto(organization);
    productsInOrganizationResponseDto =
        new ProductsInOrganizationResponseDto(organizationResponseDto, List.of(productResponseDto));
  }

  @Test
  void getAllProductsInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organizationWithProduct.getId()))
        .thenReturn(organizationWithProduct);
    when(mapper.mapToProductResponseDto(organizationWithProduct, new ArrayList<>()))
        .thenReturn(productsInOrganizationResponseDto);

    ProductsInOrganizationResponseDto products =
        productInOrganizationService.getProductsInOrganization(organizationWithProduct.getId());
    assertNotNull(products);
    assertEquals(1, products.getProducts().size());
  }

  @Test
  void createProductInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    resourceInOrganizationService.addResourceToOrganization(resourceInOrganizationRequestDto);

    when(resourceInOrganizationService.findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId()))
        .thenReturn(resourceInOrganization);

    when(mapper.mapToProductResponseDto(organization, new ArrayList<>()))
        .thenReturn(productsInOrganizationResponseDto);

    ProductsInOrganizationResponseDto products =
        productInOrganizationService.createProductInOrganization(productRequestDto);
    assertNotNull(products);
    assertEquals(1, products.getProducts().size());
    assertEquals(products.getOrganization().getId(), organization.getId());
    assertEquals(
        products.getProducts().get(0).getId(), organization.getProductsOwned().get(0).getId());
  }

  @Test
  void deleteProductInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    resourceInOrganizationService.addResourceToOrganization(resourceInOrganizationRequestDto);

    when(productService.getProduct(product.getId())).thenReturn(product);

    productInOrganizationService.deleteProductInOrganization(organization.getId(), product.getId());
    verify(productService, times(1)).deleteProductById(product.getId());
  }

  private ProductRequestDto setOwnerAndResourceToProductRequest(
      ProductRequestDto productRequestDto,
      UUID organizationId,
      UUID resourceId,
      BigDecimal quantity) {
    productRequestDto.setOwnerId(organizationId);
    ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
    resourceQuantityRequestDto.setResourceId(resourceId);
    resourceQuantityRequestDto.setQuantity(quantity);
    productRequestDto.setResourcesContent(List.of(resourceQuantityRequestDto));
    return productRequestDto;
  }

  private ProductResponseDto productToResponse(Product product) {
    ProductResponseDto productResponseDto = new ProductResponseDto();
    productResponseDto.setId(product.getId());
    return productResponseDto;
  }
}
