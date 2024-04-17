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
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.not_found.ProductNotFoundException;
import jewellery.inventory.exception.organization.MissingOrganizationPermissionException;
import jewellery.inventory.exception.organization.ProductIsNotPartOfOrganizationException;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.ProductOwnerEqualsRecipientException;
import jewellery.inventory.helper.*;
import jewellery.inventory.mapper.ProductInOrganizationMapper;
import jewellery.inventory.model.*;
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
  void transferProductShouldThrowWhenProductIsNotPartOfOrganization() {
    when(productService.getProduct(product.getId())).thenReturn(product);
    product.setOrganization(null);

    assertThrows(ProductIsNotPartOfOrganizationException.class,
            () -> productInOrganizationService.transferProduct(product.getId(), organization.getId()));
  }

  @Test
  void transferProductShouldThrowWhenOwnerOrganizationEqualsRecipient() {
    when(productService.getProduct(product.getId())).thenReturn(product);

    assertThrows(
        ProductOwnerEqualsRecipientException.class,
        () ->
            productInOrganizationService.transferProduct(
                product.getId(), product.getOrganization().getId()));
  }

  @Test
  void transferProductShouldThrowWhenProductNotFound() {
    when(productService.getProduct(any())).thenThrow(ProductNotFoundException.class);

    assertThrows(
        ProductNotFoundException.class,
        () ->
            productInOrganizationService.transferProduct(
                product.getId(), organizationWithProduct.getId()));
  }

  @Test
  void transferProductShouldThrowWhenOrganizationNotFound() {
    when(productService.getProduct(product.getId())).thenReturn(product);
    when(organizationService.getOrganization(organization.getId()))
        .thenThrow(OrganizationNotFoundException.class);

    assertThrows(
        OrganizationNotFoundException.class,
        () -> productInOrganizationService.transferProduct(product.getId(), organization.getId()));
  }

  @Test
  void transferProductShouldThrowWhenProductIsSold() {
    product.setPartOfSale(new ProductPriceDiscount());
    when(productService.getProduct(product.getId())).thenReturn(product);
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(productInOrganizationService.transferProduct(product.getId(), organization.getId()))
        .thenThrow(ProductIsSoldException.class);
    product.setOrganization(organizationWithProduct);

    assertThrows(
        ProductIsSoldException.class,
        () -> productInOrganizationService.transferProduct(product.getId(), organization.getId()));
  }

  @Test
  void transferProductShouldThrowWhenProductIsPartOfOtherProduct() {
    when(productService.getProduct(product.getId())).thenReturn(product);
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(productInOrganizationService.transferProduct(product.getId(), organization.getId()))
        .thenThrow(ProductIsContentException.class);
    product.setOrganization(organizationWithProduct);

    assertThrows(
        ProductIsContentException.class,
        () -> productInOrganizationService.transferProduct(product.getId(), organization.getId()));
  }

  @Test
  void transferProductShouldThrowWhenNoPermission() {
    when(productService.getProduct(product.getId())).thenReturn(product);
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(productInOrganizationService.transferProduct(product.getId(), organization.getId()))
        .thenThrow(MissingOrganizationPermissionException.class);
    product.setOrganization(organizationWithProduct);

    assertThrows(
        MissingOrganizationPermissionException.class,
        () -> productInOrganizationService.transferProduct(product.getId(), organization.getId()));
  }

  @Test
  void getAllProductsInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organizationWithProduct.getId()))
        .thenReturn(organizationWithProduct);
    when(mapper.mapToProductsInOrganizationResponseDto(organizationWithProduct, new ArrayList<>()))
        .thenReturn(productsInOrganizationResponseDto);

    ProductsInOrganizationResponseDto products =
        productInOrganizationService.getProductsInOrganization(organizationWithProduct.getId());

    verify(organizationService, times(1)).getOrganization(organizationWithProduct.getId());
    verify(mapper, times(1))
        .mapToProductsInOrganizationResponseDto(organizationWithProduct, new ArrayList<>());
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

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(resourceInOrganizationService, times(1))
        .findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId());
    verify(mapper, times(1))
        .mapToProductsInOrganizationResponseDto(organization, new ArrayList<>());
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
    when(productService.getProduct(product.getId())).thenReturn(product);

    ProductsInOrganizationResponseDto productsInOrganizationResponse =
        productInOrganizationService.createProductInOrganization(productWithProductRequestDto);

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(resourceInOrganizationService, times(1))
        .findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId());
    verify(mapper, times(1))
        .mapToProductsInOrganizationResponseDto(organization, new ArrayList<>());
    verify(productService, times(1)).getProduct(product.getId());
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

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(productService, times(1)).getProduct(product.getId());
    verify(resourceInOrganizationService, times(1))
        .findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId());
    verify(mapper, times(1))
        .mapToProductsInOrganizationResponseDto(organization, new ArrayList<>());
    assertNotNull(productsInOrganizationResponse);
    assertEquals(1, productsInOrganizationResponse.getProducts().size());
  }

  @Test
  void updateProductInOrganizationThrowMissingOrganizationPermissionException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);

    doThrow(MissingOrganizationPermissionException.class)
        .when(organizationService)
        .validateCurrentUserPermission(organization, OrganizationPermission.EDIT_PRODUCT);

    Assertions.assertThrows(
        MissingOrganizationPermissionException.class,
        () -> productInOrganizationService.updateProduct(product.getId(), productRequestDto));
  }

  @Test
  void updateProductInOrganizationThrowOrganizationNotOwnerException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(productService.getProduct(product.getId())).thenReturn(product);

    Assertions.assertThrows(
        OrganizationNotOwnerException.class,
        () -> productInOrganizationService.updateProduct(product.getId(), productRequestDto));
  }

  @Test
  void updateProductInOrganizationProductIsSoldException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    product.setPartOfSale(new ProductPriceDiscount());

    doThrow(ProductIsSoldException.class)
        .when(organizationService)
        .validateCurrentUserPermission(organization, OrganizationPermission.EDIT_PRODUCT);

    Assertions.assertThrows(
        ProductIsSoldException.class,
        () -> productInOrganizationService.updateProduct(product.getId(), productRequestDto));
  }

  @Test
  void deleteProductInOrganizationSuccessfully() {
    when(productService.getProduct(product.getId())).thenReturn(product);

    productInOrganizationService.deleteProductInOrganization(product.getId());

    verify(productService, times(1)).getProduct(product.getId());
    verify(productService, times(1)).deleteProductById(product.getId());
  }

  @Test
  void deleteProductInOrganizationThrowMissingOrganizationPermissionException() {
    when(productService.getProduct(product.getId())).thenReturn(product);

    doThrow(MissingOrganizationPermissionException.class)
        .when(organizationService)
        .validateCurrentUserPermission(
            organizationWithProduct, OrganizationPermission.DISASSEMBLE_PRODUCT);

    Assertions.assertThrows(
        MissingOrganizationPermissionException.class,
        () -> productInOrganizationService.deleteProductInOrganization(product.getId()));
  }

  @Test
  void deleteProductInOrganizationThrowProductIsNotPartOfOrganizationException() {
    when(productService.getProduct(product.getId())).thenReturn(product);
    product.setOrganization(null);

    Assertions.assertThrows(
        ProductIsNotPartOfOrganizationException.class,
        () -> productInOrganizationService.deleteProductInOrganization(product.getId()));
  }

  @Test
  void deleteProductInOrganizationThrowProductIsContentException() {
    when(productService.getProduct(product.getId())).thenReturn(product);
    doThrow(ProductIsContentException.class)
        .when(productService)
        .throwExceptionIfProductIsPartOfAnotherProduct(product.getId(), product);

    Assertions.assertThrows(
        ProductIsContentException.class,
        () -> productInOrganizationService.deleteProductInOrganization(product.getId()));
  }

  private ProductResponseDto productToResponse(Product product) {
    ProductResponseDto productResponseDto = new ProductResponseDto();
    productResponseDto.setId(product.getId());
    return productResponseDto;
  }
}
