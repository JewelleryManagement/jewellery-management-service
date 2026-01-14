package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.ProductTestHelper.getProductRequestDtoForOrganization;
import static jewellery.inventory.helper.ProductTestHelper.getTestProduct;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductsInOrganizationResponseDto;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.exception.not_found.ProductNotFoundException;
import jewellery.inventory.exception.organization.MissingOrganizationPermissionException;
import jewellery.inventory.exception.organization.OrganizationNotOwnerException;
import jewellery.inventory.exception.organization.ProductIsNotPartOfOrganizationException;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.ProductOwnerEqualsRecipientException;
import jewellery.inventory.helper.*;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.repository.ResourceInProductRepository;
import jewellery.inventory.service.OrganizationService;
import jewellery.inventory.service.ProductService;
import jewellery.inventory.service.ResourceInOrganizationService;
import jewellery.inventory.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductInOrganizationServiceTest {
  @InjectMocks private ProductService productService;
  @Mock private OrganizationService organizationService;
  @Mock private ProductRepository productRepository;
  @Mock private ResourceInOrganizationService resourceInOrganizationService;
  @Mock private ResourceInProductRepository resourceInProductRepository;
  @Mock private ProductMapper productMapper;
  @Mock private UserService userService;
  private static final BigDecimal QUANTITY = BigDecimal.valueOf(30);
  private Organization organization;
  private Organization organizationWithProduct;
  private ResourceInOrganization resourceInOrganization;
  private ProductRequestDto productRequestDto;
  private ProductRequestDto productWithProductRequestDto;
  private Product product;
  private Product product2;
  private User user;
  private UserInOrganization userInOrganization;
  private ProductsInOrganizationResponseDto productsInOrganizationResponseDto;

  @BeforeEach
  void setUp() {
    user = UserTestHelper.createSecondTestUser();
    organization = OrganizationTestHelper.getTestOrganization();
    userInOrganization = createUserInOrganizationAllPermissions(user, organization);
    organization.setUsersInOrganization(List.of(userInOrganization));
    Resource resource = ResourceTestHelper.getPearl();
    product = getTestProduct(user, resource);
    product2 = getTestProduct(user, resource);
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
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));
    product.setOrganization(null);

    assertThrows(
        ProductIsNotPartOfOrganizationException.class,
        () -> productService.transferProduct(product.getId(), organization.getId()));
  }

  @Test
  void transferProductShouldThrowWhenOwnerOrganizationEqualsRecipient() {
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));

    assertThrows(
        ProductOwnerEqualsRecipientException.class,
        () -> productService.transferProduct(product.getId(), product.getOrganization().getId()));
  }

  @Test
  void transferProductShouldThrowWhenProductNotFound() {
    when(productRepository.findById(any())).thenThrow(ProductNotFoundException.class);

    assertThrows(
        ProductNotFoundException.class,
        () -> productService.transferProduct(product.getId(), organizationWithProduct.getId()));
  }

  @Test
  void transferProductShouldThrowWhenOrganizationNotFound() {
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));
    when(organizationService.getOrganization(organization.getId()))
        .thenThrow(OrganizationNotFoundException.class);

    assertThrows(
        OrganizationNotFoundException.class,
        () -> productService.transferProduct(product.getId(), organization.getId()));
  }

  @Test
  void transferProductShouldThrowWhenProductIsSold() {
    product.setPartOfSale(new ProductPriceDiscount());
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));
    product.setOrganization(organizationWithProduct);

    assertThrows(
        ProductIsSoldException.class,
        () -> productService.transferProduct(product.getId(), organization.getId()));
  }

  @Test
  void transferProductShouldThrowWhenProductIsPartOfOtherProduct() {
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(productService.transferProduct(product.getId(), organization.getId()))
        .thenThrow(ProductIsContentException.class);
    product.setOrganization(organizationWithProduct);

    assertThrows(
        ProductIsContentException.class,
        () -> productService.transferProduct(product.getId(), organization.getId()));
  }

  @Test
  void transferProductShouldThrowWhenNoPermission() {
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(productService.transferProduct(product.getId(), organization.getId()))
        .thenThrow(MissingOrganizationPermissionException.class);
    product.setOrganization(organizationWithProduct);

    assertThrows(
        MissingOrganizationPermissionException.class,
        () -> productService.transferProduct(product.getId(), organization.getId()));
  }

  @Test
  void getAllProductsInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organizationWithProduct.getId()))
        .thenReturn(organizationWithProduct);
    ProductResponseDto productResponseDto = new ProductResponseDto();
    when(productMapper.mapToProductResponseDto(product)).thenReturn(productResponseDto);
    when(productMapper.mapToProductsInOrganizationResponseDto(
            organizationWithProduct, List.of(productResponseDto)))
        .thenReturn(productsInOrganizationResponseDto);
    ProductsInOrganizationResponseDto products =
        productService.getProductsInOrganization(organizationWithProduct.getId());

    verify(organizationService, times(1)).getOrganization(organizationWithProduct.getId());
    verify(productMapper, times(1))
        .mapToProductsInOrganizationResponseDto(
            organizationWithProduct, List.of(productResponseDto));
    assertNotNull(products);
    assertEquals(1, products.getProducts().size());
  }

  @Test
  void createProductInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(resourceInOrganizationService.findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId()))
        .thenReturn(resourceInOrganization);
    when(userService.getUser(user.getId())).thenReturn(user);
    ProductResponseDto productResponseDto = new ProductResponseDto();
    when(productMapper.mapToProductResponseDto(any())).thenReturn(productResponseDto);
    when(productMapper.mapToProductsInOrganizationResponseDto(
            organization, List.of(productResponseDto)))
        .thenReturn(productsInOrganizationResponseDto);

    ProductsInOrganizationResponseDto productsInOrganizationResponse =
        productService.createProductInOrganization(productRequestDto);

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(resourceInOrganizationService, times(1))
        .findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId());
    verify(productMapper, times(1))
        .mapToProductsInOrganizationResponseDto(organization, List.of(productResponseDto));
    assertNotNull(productsInOrganizationResponse);
    assertEquals(1, productsInOrganizationResponse.getProducts().size());
  }

  @Test
  void createProductWithProductInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    product.setOrganization(organization);
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));
    when(resourceInOrganizationService.findResourceInOrganizationOrThrow(
            organization,
            productWithProductRequestDto.getResourcesContent().get(0).getResourceId()))
        .thenReturn(resourceInOrganization);
    when(userService.getUser(user.getId())).thenReturn(user);
    ProductResponseDto productResponseDto = new ProductResponseDto();
    when(productMapper.mapToProductResponseDto(any())).thenReturn(productResponseDto);
    when(productMapper.mapToProductsInOrganizationResponseDto(
            organization, List.of(productResponseDto)))
        .thenReturn(productsInOrganizationResponseDto);

    ProductsInOrganizationResponseDto productsInOrganizationResponse =
        productService.createProductInOrganization(productWithProductRequestDto);

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(resourceInOrganizationService, times(1))
        .findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId());
    verify(productMapper, times(1))
        .mapToProductsInOrganizationResponseDto(organization, List.of(productResponseDto));
    verify(productRepository, times(1)).findById(product.getId());
    assertNotNull(productsInOrganizationResponse);
    assertEquals(1, productsInOrganizationResponse.getProducts().size());
  }

  @Test
  void updateProductInOrganizationSuccessfully() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    product.setOrganization(organization);
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));
    when(resourceInOrganizationService.findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId()))
        .thenReturn(resourceInOrganization);
    when(userService.getUser(user.getId())).thenReturn(user);
    when(productMapper.mapToProductsInOrganizationResponseDto(eq(organization), anyList()))
        .thenReturn(productsInOrganizationResponseDto);

    ProductsInOrganizationResponseDto productsInOrganizationResponse =
        productService.updateProduct(product.getId(), productRequestDto);

    verify(organizationService, times(1)).getOrganization(organization.getId());
    verify(productRepository, times(1)).findById(product.getId());
    verify(resourceInOrganizationService, times(1))
        .findResourceInOrganizationOrThrow(
            organization, productRequestDto.getResourcesContent().get(0).getResourceId());
    verify(productMapper, times(1))
        .mapToProductsInOrganizationResponseDto(eq(organization), anyList());
    assertNotNull(productsInOrganizationResponse);
    assertEquals(1, productsInOrganizationResponse.getProducts().size());
  }

  @Test
  void updateProductInOrganizationThrowMissingOrganizationPermissionException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);

    doThrow(MissingOrganizationPermissionException.class)
        .when(organizationService)
        .validateCurrentUserPermission(organization, OrganizationPermission.EDIT_PRODUCT);

    assertThrows(
        MissingOrganizationPermissionException.class,
        () -> productService.updateProduct(product.getId(), productRequestDto));
  }

  @Test
  void updateProductInOrganizationThrowOrganizationNotOwnerException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));
    when(userService.getUser(user.getId())).thenReturn(user);

    assertThrows(
        OrganizationNotOwnerException.class,
        () -> productService.updateProduct(product.getId(), productRequestDto));
  }

  @Test
  void updateProductInOrganizationProductIsSoldException() {
    when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
    product.setPartOfSale(new ProductPriceDiscount());

    doThrow(ProductIsSoldException.class)
        .when(organizationService)
        .validateCurrentUserPermission(organization, OrganizationPermission.EDIT_PRODUCT);

    assertThrows(
        ProductIsSoldException.class,
        () -> productService.updateProduct(product.getId(), productRequestDto));
  }

  @Test
  void deleteProductInOrganizationSuccessfully() {
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));

    productService.deleteProductInOrganization(product.getId());

    verify(productRepository, times(1)).findById(product.getId());
    verify(productRepository, times(1)).deleteById(product.getId());
  }

  @Test
  void deleteProductInOrganizationThrowMissingOrganizationPermissionException() {
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));

    doThrow(MissingOrganizationPermissionException.class)
        .when(organizationService)
        .validateCurrentUserPermission(
            organizationWithProduct, OrganizationPermission.DISASSEMBLE_PRODUCT);

    Assertions.assertThrows(
        MissingOrganizationPermissionException.class,
        () -> productService.deleteProductInOrganization(product.getId()));
  }

  @Test
  void deleteProductInOrganizationThrowProductIsNotPartOfOrganizationException() {
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));
    product.setOrganization(null);

    assertThrows(
        ProductIsNotPartOfOrganizationException.class,
        () -> productService.deleteProductInOrganization(product.getId()));
  }

  @Test
  void deleteProductInOrganizationThrowProductIsContentException() {
    product.setContentOf(product2);
    when(productRepository.findById(product.getId())).thenReturn(Optional.ofNullable(product));

    assertThrows(
        ProductIsContentException.class,
        () -> productService.deleteProductInOrganization(product.getId()));
  }

  private ProductResponseDto productToResponse(Product product) {
    ProductResponseDto productResponseDto = new ProductResponseDto();
    productResponseDto.setId(product.getId());
    return productResponseDto;
  }
}
