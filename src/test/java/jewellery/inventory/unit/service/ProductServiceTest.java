package jewellery.inventory.unit.service;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.NegativeResourceQuantityException;
import jewellery.inventory.exception.not_found.ProductNotFoundException;
import jewellery.inventory.exception.not_found.ResourceInUserNotFoundException;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.exception.product.ProductContainsException;
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


    private User user;
    private Product testProduct;
    private Pearl pearl;
    private ResourceInProduct resourceInProduct;
    private ResourceInUser resourceInUser;
    private ProductRequestDto productRequestDto;
    private ResourceInProductRequestDto resourceInProductRequestDto;

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

        resourceInProduct = new ResourceInProduct();
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

        resourceInProductRequestDto = new ResourceInProductRequestDto();
        resourceInProductRequestDto.setId(pearl.getId());
        resourceInProductRequestDto.setQuantity(5);

        productRequestDto = new ProductRequestDto();
        productRequestDto.setOwnerId(user.getId());
        productRequestDto.setName("TestProductDto");
        productRequestDto.setAuthors(List.of("Ivan", "Petar"));
        productRequestDto.setDescription("This is test product");
        productRequestDto.setResourcesContent(List.of(resourceInProductRequestDto));
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
        assertThrows(ProductContainsException.class,
                () -> productService.deleteProduct(testProduct.getId()));
    }

    @Test
    void testDeleteProductShouldThrowExceptionWhenProductIsSold() {
        testProduct.setSold(true);
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        assertThrows(ProductIsSoldException.class,
                () -> productService.deleteProduct(testProduct.getId()));
    }

    @Test
    void testDeleteProductGetProductShouldThrowExceptionWhenProductNotExist() {
        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(UUID.randomUUID()));
    }

    @Test
    void createProductSuccessfully() {

        when(userRepository.findById(productRequestDto.getOwnerId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(resourceInProductRequestDto.getId())).thenReturn(Optional.of(pearl));
        when(resourceInUserRepository.findByResourceId(pearl.getId()))
                .thenReturn(resourceInUser);


        ProductResponseDto actual = productService.createProduct(productRequestDto);

        assertEquals(actual.getName(), productRequestDto.getName());
        assertEquals(actual.getSalePrice(), productRequestDto.getSalePrice());
    }

    @Test
    void testCreateProductShouldThrowExceptionWhenResourceInUserNotFound() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        resourceInUser.setQuantity(5);
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);
        resourceInProductRequestDto.setQuantity(50);

        assertThrows(NegativeResourceQuantityException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testCreateProductShouldSetContentProduct() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(pearl.getId())).thenReturn(Optional.of(pearl));
        when(resourceInUserRepository.findByResourceId(pearl.getId())).thenReturn(resourceInUser);

        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        productRequestDto.setProductsContent(List.of(testProduct.getId()));
        ProductResponseDto actual = productService.createProduct(productRequestDto);

        assertEquals(testProduct.getContent().getId(), actual.getId());
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
    void testCreateProductGetUserShouldThrowExceptionIfUserNotExist() {
        assertThrows(UserNotFoundException.class,
                () -> productService.createProduct(productRequestDto));
    }

    @Test
    void testGetProductShouldThrowWhenProductNotFound() {
        assertThrows(ProductNotFoundException.class,
                () -> productService.getProduct(UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71")));
    }

    @Test
    void testGetProductWhenProductFound() {

        when(productRepository.findById(testProduct.getId()))
                .thenReturn(Optional.of(testProduct));

        ProductResponseDto actual = productService.getProduct(testProduct.getId());

        assertEquals(actual.getName(), testProduct.getName());

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

        assertThrows(ProductContainsException.class,
                () -> productService.deleteProduct(testProduct.getId()));
    }

    @Test
    void deleteProductShouldThrowExceptionWhenProductIsSold() {
        testProduct.setSold(true);

        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        assertThrows(ProductIsSoldException.class,
                () -> productService.deleteProduct(testProduct.getId()));
    }

    @Test
    void deleteProductShouldThrowExceptionWhenProductIdDoesNotExist() {
        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71")));
    }
}