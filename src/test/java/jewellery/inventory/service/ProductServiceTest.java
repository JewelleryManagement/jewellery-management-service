package jewellery.inventory.service;

import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.not_found.ProductNotFoundException;
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
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    UserMapper userMapper;

    private User user;
    private Product testProduct;
    private Pearl pearl;
    private ResourceInProduct resourceInProduct;
    private ResourceInUser resourceInUser;


    @BeforeEach
    void setUp() {

        user = new User();
        user.setName("Pesho");
        user.setEmail("pesho@pesho.com");

        pearl = new Pearl();
        resourceInUser = new ResourceInUser();
        resourceInUser.setOwner(user);
        resourceInUser.setResource(pearl);
        resourceInUser.setQuantity(20);

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