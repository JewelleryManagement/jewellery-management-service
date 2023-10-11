package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.product.ProductWithoutResourcesException;
import jewellery.inventory.exception.invalid_resource_quantity.NegativeResourceQuantityException;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
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
    UserMapper userMapper;
    @Mock
    ProductMapper productMapper;
    @Mock
    UserRepository userRepository;
    @Mock
    ResourceRepository resourceRepository;
    @Mock
    ResourceInUserRepository resourceInUserRepository;
    @Mock
    ResourceInProductRepository resourceInProductRepository;
    @Mock
    ResourceInUserService resourceInUserService;


    private User user;
    private Product testProduct;
    private Resource pearl;
    private ResourceInUser resourceInUser;
    private ProductRequestDto productRequestDto;
    private ResourceQuantityRequestDto resourceQuantityRequestDto;

    @BeforeEach
    void setUp() {

        user = UserTestHelper.createTestUserWithRandomId();
        pearl = ResourceTestHelper.getPearl();
        resourceInUser = createResourceInUser(user, pearl);
        testProduct = createTestProduct(user, pearl);
        resourceQuantityRequestDto = createResourceQuantityRequestDto(pearl);
        productRequestDto = createProductRequestDto(user, resourceQuantityRequestDto);
    }

    @Test
    void testDeleteProductShouldReturnResourcesInUser() {
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        productService.deleteProduct(testProduct.getId());

        assertEquals(25, resourceInUser.getQuantity());
    }

    @Test
    void testDeleteProductContainsProductBreakDown() {
        testProduct.setProductsContent(List.of(new Product(), new Product(), new Product()));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        productService.deleteProduct(testProduct.getId());

        assertEquals(0, testProduct.getProductsContent().size());
    }

    @Test
    void testDeleteProductSuccessfully() {

        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        productService.deleteProduct(testProduct.getId());

        assertEquals(0, productRepository.count());
    }


    @Test
    void testDeleteProductShouldThrowExceptionWhenProductIsPartOfProduct() {
        testProduct.setContent(new Product());
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        UUID productId = testProduct.getId();
        assertThrows(ProductIsContentException.class,
                () -> productService.deleteProduct(productId));
    }

    @Test
    void testDeleteProductShouldThrowExceptionWhenProductIsSold() {
        testProduct.setSold(true);
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        UUID productId = testProduct.getId();
        assertThrows(ProductIsSoldException.class,
                () -> productService.deleteProduct(productId));

    }

    @Test
    void testDeleteProductGetProductShouldThrowExceptionWhenProductNotExist() {
        UUID fakeId = UUID.randomUUID();
        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(fakeId));
    }

    @Test
    void createProductSuccessfully() {

        when(userRepository.findById(productRequestDto.getOwnerId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(resourceQuantityRequestDto.getId())).thenReturn(Optional.of(pearl));
        when(resourceInUserRepository.findByResourceId(pearl.getId()))
                .thenReturn(resourceInUser);
        user.setResourcesOwned(List.of(resourceInUser));

        ProductResponseDto response = new ProductResponseDto();
        when(productMapper.mapToProductResponseDto(any())).thenReturn(response);

        ProductResponseDto actual = productService.createProduct(productRequestDto);

        assertEquals(actual, response);

    }

    @Test
    void testCreateProductShouldThrowExceptionWhenResourceIsNotOwnedByUser() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        user.setResourcesOwned(null);

        assertThrows(ResourceInUserNotFoundException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductShouldRemoveResourceFromUserWhenResourceInUserIsZero() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);
        resourceInUser.setQuantity(5);
        user.setResourcesOwned(List.of(resourceInUser));
        resourceQuantityRequestDto.setQuantity(5);

        productService.createProduct(productRequestDto);

        assertEquals(0, resourceInUserRepository.count());
    }

    @Test
    void testCreateProductShouldThrowExceptionWhenResourceInUserNotFound() {
        user.setResourcesOwned(new ArrayList<>());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);

        assertThrows(ResourceInUserNotFoundException.class,
                () -> productService.createProduct(productRequestDto));

    }

    @Test
    void testCreateProductShouldThrowWhenResourceNotExistInResourceInUserRepository() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        assertThrows(ResourceInUserNotFoundException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductShouldThrowExceptionWhenResourceInUserNotEnough() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        resourceInUser.setQuantity(5);
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);
        resourceQuantityRequestDto.setQuantity(50);
        user.setResourcesOwned(List.of(resourceInUser));
        assertThrows(NegativeResourceQuantityException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductShouldThrowExceptionWhenProductToContainNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);
        user.setResourcesOwned(List.of(resourceInUser));
        productRequestDto.setProductsContent(List.of(testProduct.getId()));

        assertThrows(ProductNotFoundException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductShouldSetContentProduct() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);
        user.setResourcesOwned(List.of(resourceInUser));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        productRequestDto.setProductsContent(List.of(testProduct.getId()));

        ProductResponseDto response = new ProductResponseDto();
        when(productMapper.mapToProductResponseDto(any())).thenReturn(response);

        ProductResponseDto actual = productService.createProduct(productRequestDto);

        assertEquals(response, actual);
    }

    @Test
    void testCreateProductShouldThrowWhenProductNotFound() {
        productRequestDto.setProductsContent(List.of(UUID.randomUUID()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);
        user.setResourcesOwned(List.of(resourceInUser));

        assertThrows(ProductNotFoundException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductShouldThrowExceptionWhenResourceNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductShouldThrowWhenMissingResourcesInRequest() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        productRequestDto.setResourcesContent(null);

        assertThrows(ProductWithoutResourcesException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductGetUserShouldThrowExceptionIfUserNotExist() {
        assertThrows(UserNotFoundException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testGetProductShouldThrowWhenProductNotFound() {
        UUID fakeId = UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71");
        assertThrows(ProductNotFoundException.class,
                () -> productService.getProduct(fakeId));
    }

    @Test
    void testGetProductWhenProductFound() {

        when(productRepository.findById(testProduct.getId()))
                .thenReturn(Optional.of(testProduct));

        ProductResponseDto response = new ProductResponseDto();
        when(productMapper.mapToProductResponseDto(any())).thenReturn(response);

        ProductResponseDto actual = productService.getProduct(testProduct.getId());

        assertEquals(response, actual);

    }

    @Test
    void testGetAllProducts() {

        List<Product> products = Arrays.asList(testProduct, new Product(), new Product());

        when(productRepository.findAll()).thenReturn(products);

        List<ProductResponseDto> responses = productService.getAllProducts();

        assertEquals(products.size(), responses.size());
    }

    @Test
    void deleteProductShouldThrowExceptionWhenProductIsPartOfProduct() {

        testProduct.setContent(new Product());

        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        UUID productId = testProduct.getId();
        assertThrows(ProductIsContentException.class,
                () -> productService.deleteProduct(productId));
    }

    @Test
    void deleteProductShouldThrowExceptionWhenProductIsSold() {
        testProduct.setSold(true);

        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        UUID productId = testProduct.getId();
        assertThrows(ProductIsSoldException.class,
                () -> productService.deleteProduct(productId));
    }

    @Test
    void deleteProductShouldThrowExceptionWhenProductIdDoesNotExist() {
        UUID fakeId = UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71");
        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(fakeId));
    }
}