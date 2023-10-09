package jewellery.inventory.controller;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInProductRequestDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Gemstone;
import jewellery.inventory.repository.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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

    private static final String BASE_URL_PATH = "http://localhost:";

    private String getBaseUrl() {
        return BASE_URL_PATH + port;
    }

    private String buildUrl(String... paths) {
        return getBaseUrl() + "/" + String.join("/", paths);
    }

    private String getBaseResourceAvailabilityUrl() {
        return buildUrl("resources", "availability");
    }

    private String getBaseResourceUrl() {
        return "http://localhost:" + port + "/resources";
    }

    private String getBaseUserUrl() {
        return "http://localhost:" + port + "/users";
    }

    private String getBaseProductUrl() {
        return "http://localhost:" + port + "/products";
    }

    private String getProductUrl(UUID id) {
        return getBaseUrl() + "/products/" + id;
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

    User user;
    Gemstone gemstone;
    ResourceInUserRequestDto resourceInUserRequestDto;
    ResourcesInUserResponseDto resourcesInUser;
    ProductRequestDto productRequestDto;
    ProductResponseDto productResponseDto;

    @BeforeEach
    void setUp() {
        user = getUser();
        gemstone = getGemstone();
        resourceInUserRequestDto = getResourceInUserRequestDto(user, gemstone);
        resourcesInUser = getResourcesInUserResponseDto(resourceInUserRequestDto);
        productRequestDto = getProductRequestDto(resourcesInUser, user);
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
    void createProductSuccessfully() {

        ResponseEntity<ProductResponseDto> response =
                this.testRestTemplate.postForEntity(getBaseProductUrl(), productRequestDto, ProductResponseDto.class);
        ProductResponseDto productResponseDto = response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        assertEquals(productRequestDto.getName(), productResponseDto.getName());
        assertEquals(productRequestDto.getResourcesContent().get(0).getId(), productResponseDto.getResourcesContent().get(0).getResource().getId());
    }

    @Test
    void getProductSuccessfully() {

        productResponseDto = getProductResponseDto(productRequestDto);

        ResponseEntity<ProductResponseDto> response =
                this.testRestTemplate.getForEntity(getProductUrl(productResponseDto.getId()), ProductResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllProductsSuccessfully() {
        productResponseDto = getProductResponseDto(productRequestDto);

        ResponseEntity<List<ProductResponseDto>> response =
                this.testRestTemplate.exchange(
                        getBaseProductUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                        });

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteProductSuccessfully() {
        productResponseDto = getProductResponseDto(productRequestDto);

        ResponseEntity<HttpStatus> response =
                this.testRestTemplate.exchange(
                        getProductUrl(productResponseDto.getId()),
                        HttpMethod.DELETE,
                        null,
                        HttpStatus.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<ProductResponseDto> responseProduct =
                this.testRestTemplate.getForEntity(getProductUrl(productResponseDto.getId()), ProductResponseDto.class);

        assertEquals(HttpStatus.NOT_FOUND, responseProduct.getStatusCode());
    }


    @Nullable
    private ProductResponseDto getProductResponseDto(ProductRequestDto productRequestDto) {
        ResponseEntity<ProductResponseDto> response =
                this.testRestTemplate.postForEntity(getBaseProductUrl(), productRequestDto, ProductResponseDto.class);

        return response.getBody();
    }

    @NotNull
    private static ProductRequestDto getProductRequestDto(ResourcesInUserResponseDto resourcesInUser, User user) {
        ResourceInProductRequestDto resourceInProductRequestDto = new ResourceInProductRequestDto();
        resourceInProductRequestDto.setId(resourcesInUser.getResourcesAndQuantities().get(0).getResource().getId());
        resourceInProductRequestDto.setQuantity(5);

        ProductRequestDto productRequestDto = new ProductRequestDto();
        productRequestDto.setName("TestProduct");
        productRequestDto.setAuthors(List.of("TestAuthors"));
        productRequestDto.setDescription("Test");
        productRequestDto.setOwnerId(user.getId());
        productRequestDto.setSalePrice(50);
        productRequestDto.setResourcesContent(List.of(resourceInProductRequestDto));

        return productRequestDto;
    }

    @Nullable
    private ResourcesInUserResponseDto getResourcesInUserResponseDto(ResourceInUserRequestDto resourceInUserRequestDto) {
        ResponseEntity<ResourcesInUserResponseDto> createResourceInUser =
                this.testRestTemplate.postForEntity(getBaseResourceAvailabilityUrl(), resourceInUserRequestDto, ResourcesInUserResponseDto.class);

        return createResourceInUser.getBody();
    }

    @NotNull
    private static ResourceInUserRequestDto getResourceInUserRequestDto(User user, Gemstone gemstone) {
        ResourceInUserRequestDto resourceInUserRequestDto = new ResourceInUserRequestDto();
        resourceInUserRequestDto.setUserId(user.getId());
        resourceInUserRequestDto.setResourceId(gemstone.getId());
        resourceInUserRequestDto.setQuantity(10);
        return resourceInUserRequestDto;
    }

    @Nullable
    private Gemstone getGemstone() {
        ResourceRequestDto resourceRequest = ResourceTestHelper.getGemstoneRequestDto();
        ResponseEntity<Gemstone> createResource =
                this.testRestTemplate.postForEntity(getBaseResourceUrl(), resourceRequest, Gemstone.class);

        return createResource.getBody();
    }

    @Nullable
    private User getUser() {
        UserRequestDto userRequest = UserTestHelper.createTestUserRequest();
        ResponseEntity<User> createUser = this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, User.class);

        return createUser.getBody();
    }
}