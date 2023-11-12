package jewellery.inventory.service;

import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.exception.image.MultipartFileContentTypeException;
import jewellery.inventory.exception.image.MultipartFileNotSelectedException;
import jewellery.inventory.exception.image.MultipartFileSizeException;
import jewellery.inventory.exception.not_found.ImageNotFoundException;
import jewellery.inventory.mapper.ImageDataMapper;
import jewellery.inventory.model.Image;
import jewellery.inventory.model.Product;
import jewellery.inventory.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class ImageService {

  @Value("${image.folder.path}")
  private String folderPath;

  @Value("${multipart.file.max.size}")
  private  Integer maxFileSize;

  private static final Integer MB = 1024 * 1024;

  private final ImageRepository imageRepository;
  private final ImageDataMapper imageDataMapper;
  @Lazy
  private final ProductService productService;

  @Transactional
  public ImageResponseDto uploadImage(MultipartFile multipartFile, UUID productId)
      throws IOException {

    checkFileIsSelected(multipartFile);
    checkContentType(multipartFile);
    checkFileSize(multipartFile, maxFileSize);

    String directory = folderPath + productId;
    createDirectoryIfNotExists(directory);

    String filePath = directory + "/" + multipartFile.getOriginalFilename();

    Product product = getProduct(productId);
    Image image = createImageData(multipartFile, filePath, product);
    setProductImage(product, image);

    multipartFile.transferTo(new File(filePath));

    return imageDataMapper.toImageResponse(image);
  }

  @Transactional
  public byte[] downloadImage(UUID productId) throws IOException {
    Product product = getProduct(productId);
    checkForAttachedPicture(product);
    return Files.readAllBytes(new File(product.getImage().getFilePath()).toPath());
  }

  @Transactional
  public void deleteImage(UUID productId) throws IOException {
    removeImage(getProduct(productId));
  }

  private Image createImageData(MultipartFile file, String filePath, Product product) {
    return imageRepository.save(
        Image.builder()
            .name(file.getOriginalFilename())
            .type(file.getContentType())
            .filePath(filePath)
            .product(product)
            .build());
  }

  private void createDirectoryIfNotExists(String directory) throws IOException {
    if (Files.notExists(Path.of(directory))) {
      Files.createDirectories(Path.of(directory));
    }
  }

  private Product getProduct(UUID productId) {
    return productService.getProduct(productId);
  }

  private void setProductImage(Product product, Image image) throws IOException {
    if (product.getImage() != null) {
      removeImage(product);
    }
    product.setImage(image);
  }

  private void removeImage(Product product) throws IOException {
    checkForAttachedPicture(product);
    Image image = product.getImage();
    Files.deleteIfExists(Paths.get(product.getImage().getFilePath()));
    product.setImage(null);
    imageRepository.delete(image);
  }

  private static void checkForAttachedPicture(Product product) {
    if (product.getImage() == null) {
      throw new ImageNotFoundException(product.getId());
    }
  }

  private boolean isSupportedContentType(String contentType) {
    return contentType.equals("image/png")
        || contentType.equals("image/jpg")
        || contentType.equals("image/jpeg");
  }

  private void checkContentType(MultipartFile file) {
    if (!isSupportedContentType(Objects.requireNonNull(file.getContentType()))) {
      throw new MultipartFileContentTypeException();
    }
  }

  private static void checkFileIsSelected(MultipartFile file) {
    if (file.isEmpty()) {
      throw new MultipartFileNotSelectedException();
    }
  }

  private static void checkFileSize(MultipartFile multipartFile, Integer fileSize) {
    if (multipartFile.getSize() > (long) fileSize * MB) {
      throw new MultipartFileSizeException(fileSize);
    }
  }
}
