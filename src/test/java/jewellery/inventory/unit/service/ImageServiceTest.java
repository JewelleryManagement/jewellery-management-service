package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createTestUserWithRandomId;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;

import jewellery.inventory.exception.image.MultipartFileContentTypeException;
import jewellery.inventory.exception.image.MultipartFileNotSelectedException;
import jewellery.inventory.exception.image.MultipartFileSizeException;
import jewellery.inventory.helper.ResourceTestHelper;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.com.google.common.net.MediaType;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

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

    ReflectionTestUtils.setField(imageService, "folderPath", "/tmp/Jms/Images/");
    ReflectionTestUtils.setField(imageService, "maxFileSize", 1);
  }

  @Test
  void uploadImageShouldThrowWhenFileIsNotSelected() throws IOException {

    multipartFile = new MockMultipartFile("test.jpg", new byte[] {});
    assertThrows(
        MultipartFileNotSelectedException.class,
        () -> imageService.uploadImage(multipartFile, product.getId()));
  }

  @Test
  void uploadImageShouldThrowWhenWrongContentType() {
    multipartFile = new MockMultipartFile("test", "test.jpg", String.valueOf(MediaType.ANY_TEXT_TYPE), new byte[] {0, 0});
    assertThrows(
        MultipartFileContentTypeException.class,
        () -> imageService.uploadImage(multipartFile, product.getId()));
  }

  @Test
  void uploadImageShouldThrowWhenUploadFileIsTooBig() {
    int size = 11 * 1024 * 1024;
    byte[] largeFileContent = new byte[size];
    Arrays.fill(largeFileContent, (byte) 0);

    multipartFile = new MockMultipartFile("test", "test.jpg", "image/jpg", largeFileContent);
    assertThrows(
        MultipartFileSizeException.class,
        () -> imageService.uploadImage(multipartFile, product.getId()));
  }
}
