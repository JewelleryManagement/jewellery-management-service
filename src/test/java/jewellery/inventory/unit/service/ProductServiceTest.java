package jewellery.inventory.unit.service;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.NegativeResourceQuantityException;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Pearl;
import jewellery.inventory.model.resource.ResourceInProduct;
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
    private Pearl pearl;
    private ResourceInUser resourceInUser;
    private ProductRequestDto productRequestDto;
    private ResourceQuantityRequestDto resourceQuantityRequestDto;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Pesho");
        user.setEmail("pesho@pesho.com");

        pearl = new Pearl();
        pearl.setId(UUID.randomUUID());

        resourceInUser = new ResourceInUser();
        resourceInUser.setId(UUID.randomUUID());
        resourceInUser.setOwner(user);
        resourceInUser.setResource(pearl);
        resourceInUser.setQuantity(20);
        user.setResourcesOwned(List.of(resourceInUser));

        ResourceInProduct resourceInProduct = new ResourceInProduct();
        resourceInProduct.setResource(pearl);
        resourceInProduct.setQuantity(5);

        testProduct = new Product();
        testProduct.setId(UUID.randomUUID());
        testProduct.setOwner(user);
        testProduct.setName("TestProduct");
        testProduct.setAuthors(List.of("Gosho"));
        testProduct.setSold(false);
        testProduct.setDescription("This is Test Product");
        testProduct.setSalePrice(1000);
        testProduct.setResourcesContent(List.of(resourceInProduct));
        testProduct.setProductsContent(null);
        testProduct.setContent(null);

        resourceQuantityRequestDto = new ResourceQuantityRequestDto();
        resourceQuantityRequestDto.setId(pearl.getId());
        resourceQuantityRequestDto.setQuantity(5);

        productRequestDto = new ProductRequestDto();
        productRequestDto.setOwnerId(user.getId());
        productRequestDto.setName("TestProductDto");
        productRequestDto.setAuthors(List.of("Ivan", "Petar"));
        productRequestDto.setDescription("This is test product");
        productRequestDto.setResourcesContent(List.of(resourceQuantityRequestDto));
        productRequestDto.setSalePrice(10000);

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

        assertThrows(NegativeResourceQuantityException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductShouldThrowExceptionWhenProductToContainNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);

        productRequestDto.setProductsContent(List.of(testProduct.getId()));

        assertThrows(ProductNotFoundException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductShouldSetContentProduct() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);

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