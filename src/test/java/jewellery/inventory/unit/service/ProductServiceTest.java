package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createTestUserWithRandomId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.ProductOwnerEqualsRecipientException;
import jewellery.inventory.exception.product.ProductOwnerNotSeller;
import jewellery.inventory.helper.ProductTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.*;
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
            ProductOwnerNotSeller.class, () -> productService.createProduct(productRequestDto));
  }

  @Test
  void testTransferProductThrowsExceptionWhenProductIsContent() {
    Product contentProduct = getTestProduct(user, pearl);
    contentProduct.setContentOf(product);
    when(productRepository.findById(contentProduct.getId()))
        .thenReturn(Optional.of(contentProduct));

    assertThrows(
        ProductIsContentException.class,
        () -> productService.transferProduct(user.getId(), contentProduct.getId()));

    assertNotNull(contentProduct.getContentOf());
  }

  @Test
  void testTransferProductThrowsExceptionWhenProductIsSold() {
    product.setPartOfSale(new Sale());
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    assertThrows(
        ProductIsSoldException.class,
        () -> productService.transferProduct(user.getId(), product.getId()));

    assertNotNull(product.getPartOfSale());
  }

  @Test
  void testTransferProductThrowsExceptionWhenProductOwnerEqualsRecipient() {
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    assertThrows(
        ProductOwnerEqualsRecipientException.class,
        () -> productService.transferProduct(user.getId(), product.getId()));

    assertEquals(product.getOwner().getId(), user.getId());
  }

  @Test
  void testTransferProductWhenDataIsCorrect() {
    User recipient = createTestUserWithRandomId();
    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
    when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
    assertEquals(product.getOwner().getId(), user.getId());

    productService.transferProduct(recipient.getId(), product.getId());

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
    assertThrows(ProductNotFoundException.class, () -> productService.getProduct(fakeId));
  }

  @Test
  void testGetProductWhenProductFound() {

    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    ProductResponseDto response = new ProductResponseDto();
    when(productMapper.mapToProductResponseDto(any())).thenReturn(response);

    ProductResponseDto actual = productService.getProduct(product.getId());

    assertEquals(response, actual);
    assertEquals(response.getId(), actual.getId());
    assertEquals(response.getAuthors(), actual.getAuthors());
    assertEquals(response.getCatalogNumber(), actual.getCatalogNumber());
  }

  @Test
  void testGetAllProducts() {

    List<Product> products = Arrays.asList(product, new Product(), new Product());

    when(productRepository.findAll()).thenReturn(products);

    List<ProductResponseDto> responses = productService.getAllProducts();

    assertEquals(products.size(), responses.size());
  }

  @Test
  void testDeleteProductSuccessfully() {

    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    productService.deleteProduct(product.getId());

    assertEquals(0, productRepository.count());
    verify(productRepository, times(1)).deleteById(product.getId());
  }

  @Test
  void testDeleteProductDisassembleContentProduct() {
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
    product.setPartOfSale(new Sale());
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
    product.setPartOfSale(new Sale());

    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
    UUID productId = product.getId();
    assertThrows(ProductIsSoldException.class, () -> productService.deleteProduct(productId));
  }

  @Test
  void deleteProductShouldThrowExceptionWhenProductIdDoesNotExist() {
    UUID fakeId = UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71");
    assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(fakeId));
  }
}
