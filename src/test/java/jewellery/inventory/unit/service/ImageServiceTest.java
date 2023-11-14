package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createTestUserWithRandomId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import jewellery.inventory.exception.image.MultipartFileContentTypeException;
import jewellery.inventory.exception.image.MultipartFileNotSelectedException;
import jewellery.inventory.exception.image.MultipartFileSizeException;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.helper.mocks.CorrectImageMockData;
import jewellery.inventory.helper.mocks.EmptyImageMockData;
import jewellery.inventory.helper.mocks.HugeImageMockDataWith;
import jewellery.inventory.helper.mocks.WrongContentTypeImageMockData;
import jewellery.inventory.mapper.ImageDataMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ImageRepository;
import jewellery.inventory.service.ImageService;
import jewellery.inventory.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

  @InjectMocks ImageService imageService;
  @Mock ImageRepository imageRepository;
  @Mock ProductService productService;
  @Mock private ImageDataMapper imageDataMapper;

  private User user;
  private Product product;
  private Resource pearl;
  private MultipartFile multipartFile;

  @BeforeEach
  void setUp() {
    user = createTestUserWithRandomId();
    pearl = ResourceTestHelper.getPearl();
    product = getTestProduct(user, pearl);

    ReflectionTestUtils.setField(imageService, "maxFileSize", 1);
  }

  @Test
  void uploadImageShouldThrowWhenFileIsNotSelected() throws IOException {

    multipartFile = new EmptyImageMockData();
    assertThrows(
        MultipartFileNotSelectedException.class,
        () -> imageService.uploadImage(multipartFile, product.getId()));
  }

  @Test
  void uploadImageShouldThrowWhenWrongContentType() {
    multipartFile = new WrongContentTypeImageMockData();
    assertThrows(
        MultipartFileContentTypeException.class,
        () -> imageService.uploadImage(multipartFile, product.getId()));
  }

  @Test
  void uploadImageShouldThrowWhenUploadFileIsTooBig() {
    multipartFile = new HugeImageMockDataWith();
    assertThrows(
        MultipartFileSizeException.class,
        () -> imageService.uploadImage(multipartFile, product.getId()));
  }

  @Test
  void uploadImageSuccessfully() throws IOException {
    when(productService.getProduct(product.getId())).thenReturn(product);
    multipartFile = new CorrectImageMockData();
    imageService.uploadImage(multipartFile, product.getId());
  }
}
