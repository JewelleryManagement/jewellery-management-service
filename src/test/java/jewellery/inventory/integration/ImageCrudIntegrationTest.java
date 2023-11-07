package jewellery.inventory.integration;

import jewellery.inventory.dto.request.ImageRequestDto;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.repository.ImageRepository;
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ImageCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

    private static final String FILE_PATH = "C:\\Users\\Gatio\\Desktop\\jewellery-managemen\\jewellery-management-service\\src\\test\\resources\\pearl.jpg";

    private String getBaseUrl() {
        return BASE_URL_PATH + port;
    }

    private String getBaseProductUrl() {
        return getBaseUrl() + "/products";
    }

    private String getBaseImageUrl(UUID productId) {
        return getBaseProductUrl() + "/" + productId + "/picture";
    }

    @Autowired private ImageRepository imageRepository;
    @Autowired private ProductRepository productRepository;
    @MockBean private ProductService productService;


    ProductResponseDto productResponseDto;
    ImageRequestDto imageRequestDto;

    @BeforeEach
    void setup() throws IOException {
        imageRepository.deleteAll();
        productRepository.deleteAll();

        productResponseDto = createTestProductResponse(createTestProductRequest());
        imageRequestDto = createImageRequest(createTestImage());
    }

    @Test
    void uploadImageSuccessfullyAndAttachToProduct() {
        ResponseEntity<ImageResponseDto> response =
                this.testRestTemplate.postForEntity(
                        getBaseImageUrl(productResponseDto.getId()), imageRequestDto, ImageResponseDto.class);
    }

    private ProductResponseDto createTestProductResponse(ProductRequestDto productRequestDto) {
        ResponseEntity<ProductResponseDto> response =
                this.testRestTemplate.postForEntity(
                        getBaseProductUrl(), productRequestDto, ProductResponseDto.class);
        return response.getBody();
    }

    private ProductRequestDto createTestProductRequest() {
        ProductRequestDto productRequestDto = new ProductRequestDto();
        productRequestDto.setOwnerId(UUID.randomUUID());
        productRequestDto.setAuthors(List.of(UUID.randomUUID()));
        productRequestDto.setResourcesContent(List.of(createTestResourceRequest()));
        return productRequestDto;
    }

    private ResourceQuantityRequestDto createTestResourceRequest() {
        ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
        resourceQuantityRequestDto.setId(UUID.randomUUID());
        resourceQuantityRequestDto.setQuantity(5);
        return resourceQuantityRequestDto;
    }

    private MockMultipartFile createTestImage() throws IOException {
        return new MockMultipartFile(
                "image",
                "pearl.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                FILE_PATH.getBytes());
    }

    private ImageRequestDto createImageRequest(MultipartFile multipartFile) {
        ImageRequestDto requestDto = new ImageRequestDto();
        requestDto.setImage(multipartFile);
        return requestDto;
    }
}
