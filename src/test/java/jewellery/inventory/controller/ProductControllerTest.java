package jewellery.inventory.controller;

import static jewellery.inventory.controller.ProductTestHelper.*;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Pearl;
import jewellery.inventory.model.resource.ResourceInProduct;
import jewellery.inventory.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductControllerTest {

    @Value(value = "${local.server.port}")
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/products";
    }

    private String getProductUrl(UUID id) {
        return getBaseUrl() + "/" + id;
    }

    @Autowired
    TestRestTemplate testRestTemplate;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    ResourceInUserRepository resourceInUserRepository;
    @Autowired
    ResourceInProductRepository resourceInProductRepository;

    private User user;
    private Pearl pearl;
    private ResourceInUser resourceInUser;
    private ResourceInProductRequestDto resourceInProductRequestDto;
    private ResourceInProduct resourceInProduct;

    @BeforeEach
    void setUp() {
        user = UserTestHelper.createTestUserWithRandomId();
        pearl = getPearl();
        resourceInUser = getResourceInUser(user, pearl);
        user.setResourcesOwned(List.of(resourceInUser));
        resourceInProductRequestDto = getResourceInProductRequestDto(pearl);
        resourceInProduct = getResourceInProduct(pearl);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        productRepository.deleteAll();
        resourceRepository.deleteAll();
        resourceInUserRepository.deleteAll();
        resourceInProductRepository.deleteAll();
    }

    @Test
    void getProduct() {

        Product product = ProductTestHelper.getProduct(user, List.of(resourceInProduct));

        ResponseEntity<ProductResponseDto> response =
                this.testRestTemplate.getForEntity(getProductUrl(product.getId()), ProductResponseDto.class);

        System.out.println(response.getStatusCode());
    }

    @Test
    void createProduct() {

        ProductRequestDto productRequestDto = getProductRequest(user, resourceInProductRequestDto);

        ResponseEntity<ProductResponseDto> response =
                this.testRestTemplate.postForEntity(getBaseUrl(), productRequestDto, ProductResponseDto.class);

        System.out.println(productRequestDto.getName());
        System.out.println(productRequestDto.getAuthors());
        System.out.println(productRequestDto.getDescription());

        System.out.println(response.getStatusCode());
        System.out.println(getBaseUrl());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ProductResponseDto productResponse = response.getBody();

        assertNotNull(productResponse);
        assertNotNull(productResponse.getId());
        assertEquals(productRequestDto.getName(), productResponse.getName());
        assertEquals(productRequestDto.getDescription(), productResponse.getDescription());
    }
}