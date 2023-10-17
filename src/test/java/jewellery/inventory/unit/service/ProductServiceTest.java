package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.UserNotOwnerException;
import jewellery.inventory.helper.ProductTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private ResourceInUserRepository resourceInUserRepository;
    @Mock
    private ResourceInProductRepository resourceInProductRepository;
    @Mock
    private ResourceInUserService resourceInUserService;

    private User user;
    private Product testContentProduct;
    private Resource pearl;
    private ResourceInUser resourceInUser;
    private ProductRequestDto productRequestDto;

    @BeforeEach
    void setUp() {
        user = UserTestHelper.createTestUserWithRandomId();
        pearl = ResourceTestHelper.getPearl();
        resourceInUser = getResourceInUser(user, pearl);
        testContentProduct = getTestProduct(user, pearl);
        productRequestDto = ProductTestHelper.getProductRequestDto(user, getResourceQuantityRequestDto(pearl));
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
    void testCreateProductShouldThrowWhenProductOwnerIsNotTheSameAsContentProductOwner() {
        when(userRepository.findById(productRequestDto.getOwnerId())).thenReturn(Optional.of(user));
        when(productRepository.findById(testContentProduct.getId())).thenReturn(Optional.of(testContentProduct));

        User anotherUser = UserTestHelper.createTestUserWithRandomId();
        testContentProduct.setOwner(anotherUser);
        productRequestDto.setProductsContent(List.of(testContentProduct.getId()));

        assertThrows(UserNotOwnerException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductShouldSetContentProduct() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceInUserRepository.findByResourceIdAndOwnerId(pearl.getId(), user.getId()))
                .thenReturn(Optional.of(resourceInUser));
        user.setResourcesOwned(List.of(resourceInUser));
        when(productRepository.findById(testContentProduct.getId())).thenReturn(Optional.of(testContentProduct));

        productRequestDto.setProductsContent(List.of(testContentProduct.getId()));

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

        when(productRepository.findById(testContentProduct.getId())).thenReturn(Optional.of(testContentProduct));

        ProductResponseDto response = new ProductResponseDto();
        when(productMapper.mapToProductResponseDto(any())).thenReturn(response);

        ProductResponseDto actual = productService.getProduct(testContentProduct.getId());

        assertEquals(response, actual);
        assertEquals(response.getId(), actual.getId());
        assertEquals(response.getAuthors(), actual.getAuthors());
        assertEquals(response.getCatalogNumber(), actual.getCatalogNumber());
    }

    @Test
    void testGetAllProducts() {

        List<Product> products = Arrays.asList(testContentProduct, new Product(), new Product());

        when(productRepository.findAll()).thenReturn(products);

        List<ProductResponseDto> responses = productService.getAllProducts();

        assertEquals(products.size(), responses.size());
    }

    @Test
    void testDeleteProductSuccessfully() {

        when(productRepository.findById(testContentProduct.getId())).thenReturn(Optional.of(testContentProduct));

        productService.deleteProduct(testContentProduct.getId());

        assertEquals(0, productRepository.count());
        verify(productRepository, times(1)).deleteById(testContentProduct.getId());
    }

    @Test
    void testDeleteProductDisassembleContentProduct() {
        Product content1 = getTestProduct(user, pearl);
        Product content2 = getTestProduct(user, pearl);

        testContentProduct.setProductsContent(List.of(content1, content2));
        when(productRepository.findById(testContentProduct.getId())).thenReturn(Optional.of(testContentProduct));

        productService.deleteProduct(testContentProduct.getId());

        verify(productRepository, times(1)).deleteById(testContentProduct.getId());
        verify(productRepository, times(1)).save(content1);
        verify(productRepository, times(1)).save(content2);
    }

    @Test
    void testDeleteProductShouldThrowExceptionWhenProductIsPartOfProduct() {
        testContentProduct.setContentOf(new Product());
        when(productRepository.findById(testContentProduct.getId())).thenReturn(Optional.of(testContentProduct));
        UUID productId = testContentProduct.getId();
        assertThrows(ProductIsContentException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    void testDeleteProductShouldThrowExceptionWhenProductIsSold() {
        testContentProduct.setSold(true);
        when(productRepository.findById(testContentProduct.getId())).thenReturn(Optional.of(testContentProduct));
        UUID productId = testContentProduct.getId();
        assertThrows(ProductIsSoldException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    void testDeleteProductGetProductShouldThrowExceptionWhenProductNotExist() {
        UUID fakeId = UUID.randomUUID();
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(fakeId));
    }

    @Test
    void deleteProductShouldThrowExceptionWhenProductIsPartOfProduct() {

        testContentProduct.setContentOf(new Product());

        when(productRepository.findById(testContentProduct.getId())).thenReturn(Optional.of(testContentProduct));
        UUID productId = testContentProduct.getId();
        assertThrows(ProductIsContentException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    void deleteProductShouldThrowExceptionWhenProductIsSold() {
        testContentProduct.setSold(true);

        when(productRepository.findById(testContentProduct.getId())).thenReturn(Optional.of(testContentProduct));
        UUID productId = testContentProduct.getId();
        assertThrows(ProductIsSoldException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    void deleteProductShouldThrowExceptionWhenProductIdDoesNotExist() {
        UUID fakeId = UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71");
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(fakeId));
    }
}
