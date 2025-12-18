package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createTestUserWithRandomId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.*;
import jewellery.inventory.helper.ProductTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.ImageService;
import jewellery.inventory.service.ProductService;
import jewellery.inventory.service.ResourceInUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @InjectMocks private ProductService productService;
  @Mock private ProductRepository productRepository;
  @Mock private UserMapper userMapper;
  @Mock private ProductMapper productMapper;
  @Mock private UserRepository userRepository;
  @Mock private ResourceRepository resourceRepository;
  @Mock private ResourceInUserRepository resourceInUserRepository;
  @Mock private ResourceInProductRepository resourceInProductRepository;
  @Mock private ResourceInUserService resourceInUserService;
  @Mock private ImageService imageService;

  private User user;
  private Product product;
  private Resource pearl;
  private ResourceInUser resourceInUser;
  private ProductRequestDto productRequestDto;

  @BeforeEach
  void setUp() {
    user = createTestUserWithRandomId();
    pearl = ResourceTestHelper.getPearl();
    resourceInUser = getResourceInUser(user, pearl);
    product = getTestProduct(user, pearl);
    productRequestDto =
        ProductTestHelper.getProductRequestDto(user, getResourceQuantityRequestDto(pearl));
  }

  @Test
  void testCreateProductShouldThrowWhenProductOwnerIsNotTheSameAsContentProductOwner() {
    when(userRepository.findById(productRequestDto.getOwnerId())).thenReturn(Optional.of(user));
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    User anotherUser = createTestUserWithRandomId();
    product.setOwner(anotherUser);
    productRequestDto.setProductsContent(List.of(product.getId()));

    assertThrows(
        UserNotOwnerException.class, () -> productService.createProduct(productRequestDto));
  }

  @Test
  void testTransferProductThrowsExceptionWhenProductIsContent() {
    Product contentProduct = getTestProduct(user, pearl);
    contentProduct.setContentOf(product);
    when(productRepository.findById(contentProduct.getId()))
        .thenReturn(Optional.of(contentProduct));

    assertThrows(
        ProductIsContentException.class,
        () -> productService.transferProduct(contentProduct.getId(), user.getId()));

    assertNotNull(contentProduct.getContentOf());
  }

  @Test
  void testTransferProductThrowsExceptionWhenProductIsSold() {
    product.setPartOfSale(new ProductPriceDiscount());
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    assertThrows(
        ProductIsSoldException.class,
        () -> productService.transferProduct(product.getId(), user.getId()));

    assertNotNull(product.getPartOfSale());
  }

  @Test
  void testTransferProductThrowsExceptionWhenProductOwnerEqualsRecipient() {
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    assertThrows(
        ProductOwnerEqualsRecipientException.class,
        () -> productService.transferProduct(product.getId(), user.getId()));

    assertEquals(product.getOwner().getId(), user.getId());
  }

  @Test
  void testTransferProductWhenDataIsCorrect() {
    User recipient = createTestUserWithRandomId();
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
    when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
    assertEquals(product.getOwner().getId(), user.getId());

    productService.transferProduct(product.getId(), recipient.getId());

    assertNotEquals(recipient.getId(), user.getId());
    assertNull(product.getPartOfSale());
    assertNull(product.getContentOf());
  }

  @Test
  void createProductSuccessfully() {

    when(userRepository.findById(productRequestDto.getOwnerId())).thenReturn(Optional.of(user));
    when(resourceInUserRepository.findByResourceIdAndOwnerId(pearl.getId(), user.getId()))
        .thenReturn(Optional.of(resourceInUser));
    user.setResourcesOwned(List.of(resourceInUser));

    ProductResponseDto response = new ProductResponseDto();
    when(productMapper.mapToProductResponseDto(any())).thenReturn(response);

    ProductResponseDto actual = productService.createProduct(productRequestDto);

    assertEquals(actual, response);
    assertEquals(actual.getProductionNumber(), response.getProductionNumber());
    assertEquals(actual.getCatalogNumber(), response.getProductionNumber());
  }

  @Test
  void testCreateProductShouldThrowWhenProductNotFound() {
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    user.setResourcesOwned(List.of(resourceInUser));
    productRequestDto.setProductsContent(List.of(UUID.randomUUID()));

    assertThrows(
        ProductNotFoundException.class, () -> productService.createProduct(productRequestDto));
  }

  @Test
  void testCreateProductShouldSetContentProduct() {

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(resourceInUserRepository.findByResourceIdAndOwnerId(pearl.getId(), user.getId()))
        .thenReturn(Optional.of(resourceInUser));
    user.setResourcesOwned(List.of(resourceInUser));
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    productRequestDto.setProductsContent(List.of(product.getId()));

    ProductResponseDto response = new ProductResponseDto();
    when(productMapper.mapToProductResponseDto(any())).thenReturn(response);

    ProductResponseDto actual = productService.createProduct(productRequestDto);

    assertEquals(response, actual);
    assertEquals(response.getContentOf(), actual.getContentOf());
    assertEquals(response.getProductsContent(), actual.getProductsContent());
  }

  @Test
  void testCreateProductShouldThrowExceptionWhenResourceNotFound() {
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    assertThrows(
        ResourceInUserNotFoundException.class,
        () -> productService.createProduct(productRequestDto));
  }

  @Test
  void testCreateProductGetUserShouldThrowExceptionIfUserNotExist() {
    assertThrows(
        UserNotFoundException.class, () -> productService.createProduct(productRequestDto));
  }

  @Test
  void testGetProductShouldThrowWhenProductNotFound() {
    UUID fakeId = UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71");
    assertThrows(ProductNotFoundException.class, () -> productService.getProductResponse(fakeId));
  }

  @Test
  void testGetProductWhenProductFound() {

    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    ProductResponseDto response = new ProductResponseDto();
    when(productMapper.mapToProductResponseDto(any())).thenReturn(response);

    ProductResponseDto actual = productService.getProductResponse(product.getId());

    assertEquals(response, actual);
    assertEquals(response.getId(), actual.getId());
    assertEquals(response.getAuthors(), actual.getAuthors());
    assertEquals(response.getCatalogNumber(), actual.getCatalogNumber());
  }

  @Test
  void testDeleteProductSuccessfully() throws IOException {

    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    productService.deleteProduct(product.getId());

    assertEquals(0, productRepository.count());
    verify(productRepository, times(1)).deleteById(product.getId());
  }

  @Test
  void testDeleteProductDisassembleContentProduct() throws IOException {
    Product content1 = getTestProduct(user, pearl);
    Product content2 = getTestProduct(user, pearl);

    product.setProductsContent(List.of(content1, content2));
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    productService.deleteProduct(product.getId());

    verify(productRepository, times(1)).deleteById(product.getId());
    verify(productRepository, times(1)).save(content1);
    verify(productRepository, times(1)).save(content2);
  }

  @Test
  void testDeleteProductShouldThrowExceptionWhenProductIsPartOfProduct() {
    product.setContentOf(new Product());
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
    UUID productId = product.getId();
    assertThrows(ProductIsContentException.class, () -> productService.deleteProduct(productId));
  }

  @Test
  void testDeleteProductShouldThrowExceptionWhenProductIsSold() {
    product.setPartOfSale(new ProductPriceDiscount());
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
    UUID productId = product.getId();
    assertThrows(ProductIsSoldException.class, () -> productService.deleteProduct(productId));
  }

  @Test
  void testDeleteProductGetProductShouldThrowExceptionWhenProductNotExist() {
    UUID fakeId = UUID.randomUUID();
    assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(fakeId));
  }

  @Test
  void deleteProductShouldThrowExceptionWhenProductIsPartOfProduct() {

    product.setContentOf(new Product());

    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
    UUID productId = product.getId();
    assertThrows(ProductIsContentException.class, () -> productService.deleteProduct(productId));
  }

  @Test
  void deleteProductShouldThrowExceptionWhenProductIsSold() {
    product.setPartOfSale(new ProductPriceDiscount());
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
    UUID productId = product.getId();
    assertThrows(ProductIsSoldException.class, () -> productService.deleteProduct(productId));
  }

  @Test
  void deleteProductShouldThrowExceptionWhenProductIdDoesNotExist() {
    UUID fakeId = UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71");
    assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(fakeId));
  }

  @Test
  void updateProductShouldThrowWhenProductNotFound() {
    assertThrows(
        ProductNotFoundException.class,
        () -> productService.updateProduct(product.getId(), productRequestDto));
  }

  @Test
  void updateProductShouldThrowWhenProductIsSold() {
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
    product.setPartOfSale(new ProductPriceDiscount());
    assertThrows(
        ProductIsSoldException.class,
        () -> productService.updateProduct(product.getId(), productRequestDto));
  }

  @Test
  void testUpdateProductShouldThrowWhenProductIsPartOfItself() {
    productRequestDto.setProductsContent(List.of(product.getId()));
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    assertThrows(
        ProductPartOfItselfException.class,
        () -> productService.updateProduct(product.getId(), productRequestDto));
  }

  @Test
  void testUpdateProductSuccessfullyWhenProductIsPartOfAnotherProduct() {
    Product innerProduct = getTestProduct(user, pearl);
    innerProduct.setContentOf(product);
    when(productRepository.findById(innerProduct.getId())).thenReturn(Optional.of(innerProduct));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(resourceInUserRepository.findByResourceIdAndOwnerId(pearl.getId(), user.getId()))
        .thenReturn(Optional.of(resourceInUser));

    productService.updateProduct(innerProduct.getId(), productRequestDto);

    verify(productRepository, times(1)).findById(innerProduct.getId());
    verify(userRepository, times(1)).findById(user.getId());
    verify(resourceInUserRepository, times(1))
        .findByResourceIdAndOwnerId(pearl.getId(), user.getId());
    verify(productRepository, times(2)).save(innerProduct);
  }

  @Test
  void updateInnerProductShouldThrowWhenProductIsSold() {
    product.setPartOfSale(new ProductPriceDiscount());

    Product innerProduct = getTestProduct(user, pearl);
    innerProduct.setContentOf(product);

    when(productRepository.findById(innerProduct.getId())).thenReturn(Optional.of(innerProduct));
    assertThrows(
        ProductIsSoldException.class,
        () -> productService.updateProduct(innerProduct.getId(), productRequestDto));
  }
}
