package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.ProductTestHelper.getProductRequestDtoForOrganization;
import static jewellery.inventory.helper.ProductTestHelper.getTestProduct;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductsInOrganizationResponseDto;
import jewellery.inventory.helper.*;
import jewellery.inventory.mapper.ProductInOrganizationMapper;
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
  @Mock private ProductRepository productRepository;
  @Mock private ProductInOrganizationMapper mapper;
  @Mock private ResourceInOrganizationService resourceInOrganizationService;
  @Mock private ResourceInProductRepository resourceInProductRepository;
  private static final BigDecimal QUANTITY = BigDecimal.valueOf(30);

  private Organization organization;
  private Organization organizationWithProduct;
  private ResourceInOrganization resourceInOrganization;
  private ProductRequestDto productRequestDto;
  private ProductRequestDto productWithProductRequestDto;
  private Product product;
  private ProductsInOrganizationResponseDto productsInOrganizationResponseDto;

  @BeforeEach
  void setUp() {

    User user = UserTestHelper.createSecondTestUser();
    organization = OrganizationTestHelper.getTestOrganization();
    Resource resource = ResourceTestHelper.getPearl();
    product = getTestProduct(user, resource);
    resourceInOrganization =
        ResourceInOrganizationTestHelper.createResourceInOrganization(organization, resource);
    organization.setResourceInOrganization(List.of(resourceInOrganization));
    productRequestDto =
        getProductRequestDtoForOrganization(user, organization.getId(), user.getId(), QUANTITY);
    productWithProductRequestDto =
        getProductRequestDtoForOrganization(user, organization.getId(), user.getId(), QUANTITY);
    productWithProductRequestDto.setProductsContent(List.of(product.getId()));
    organizationWithProduct =
        setProductAndResourcesToOrganization(
            OrganizationTestHelper.getTestOrganization(), product, resourceInOrganization);
    productsInOrganizationResponseDto =
        new ProductsInOrganizationResponseDto(
            getTestOrganizationResponseDto(OrganizationTestHelper.getTestOrganization()),
            List.of(productToResponse(product)));
  }

  @Test
  void getAllProductsInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organizationWithProduct.getId()))
        .thenReturn(organizationWithProduct);
    when(mapper.mapToProductsInOrganizationResponseDto(organizationWithProduct, new ArrayList<>()))
        .thenReturn(productsInOrganizationResponseDto);

    ProductsInOrganizationResponseDto products =
        productInOrganizationService.getProductsInOrganization(organizationWithProduct.getId());

    assertNotNull(products);
    assertEquals(1, products.getProducts().size());
  }

  @Test
  void createProductInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(resourceInOrganizationService.findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId()))
        .thenReturn(resourceInOrganization);
    when(mapper.mapToProductsInOrganizationResponseDto(organization, new ArrayList<>()))
        .thenReturn(productsInOrganizationResponseDto);

    ProductsInOrganizationResponseDto productsInOrganizationResponse =
        productInOrganizationService.createProductInOrganization(productRequestDto);

    assertNotNull(productsInOrganizationResponse);
    assertEquals(1, productsInOrganizationResponse.getProducts().size());
  }

  @Test
  void createProductWithProductInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(resourceInOrganizationService.findResourceInOrganizationOrThrow(
            organization,
            productWithProductRequestDto.getResourcesContent().get(0).getResourceId()))
        .thenReturn(resourceInOrganization);
    product.setOrganization(organization);
    when(mapper.mapToProductsInOrganizationResponseDto(organization, new ArrayList<>()))
        .thenReturn(productsInOrganizationResponseDto);
    when(productService.getProduct(any(UUID.class))).thenReturn(product);

    ProductsInOrganizationResponseDto productsInOrganizationResponse =
        productInOrganizationService.createProductInOrganization(productWithProductRequestDto);

    assertNotNull(productsInOrganizationResponse);
    assertEquals(1, productsInOrganizationResponse.getProducts().size());
  }

  @Test
  void updateProductInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    product.setOrganization(organization);
    when(productService.getProduct(product.getId())).thenReturn(product);
    when(resourceInOrganizationService.findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId()))
        .thenReturn(resourceInOrganization);
    when(mapper.mapToProductsInOrganizationResponseDto(eq(organization), anyList()))
        .thenReturn(productsInOrganizationResponseDto);

    ProductsInOrganizationResponseDto productsInOrganizationResponse =
        productInOrganizationService.updateProduct(product.getId(), productRequestDto);

    assertNotNull(productsInOrganizationResponse);
    assertEquals(1, productsInOrganizationResponse.getProducts().size());
  }

  @Test
  void deleteProductInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organizationWithProduct.getId()))
        .thenReturn(organizationWithProduct);
    when(productService.getProduct(product.getId())).thenReturn(product);

    productInOrganizationService.deleteProductInOrganization(product.getId());

    verify(productService, times(1)).deleteProductById(product.getId());
  }

  private ProductResponseDto productToResponse(Product product) {
    ProductResponseDto productResponseDto = new ProductResponseDto();
    productResponseDto.setId(product.getId());
    return productResponseDto;
  }
}
