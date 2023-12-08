package jewellery.inventory.service;

import static jewellery.inventory.model.Image.FILE_NAME;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.exception.image.MultipartFileContentTypeException;
import jewellery.inventory.exception.image.MultipartFileNotSelectedException;
import jewellery.inventory.exception.image.MultipartFileSizeException;
import jewellery.inventory.exception.not_found.ImageNotFoundException;
import jewellery.inventory.mapper.ImageDataMapper;
import jewellery.inventory.model.Image;
import jewellery.inventory.model.Product;
import jewellery.inventory.repository.ImageRepository;
import jewellery.inventory.service.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class ImageService {
  private static final Logger logger = LogManager.getLogger(ImageService.class);

  @Value("${image.folder.path}")
  private String folderPath;

  @Value("${multipart.file.max.size}")
  private Integer maxFileSize;

  private static final Integer MB = 1024 * 1024;

  private final ImageRepository imageRepository;
  private final ImageDataMapper imageDataMapper;
  @Lazy private final ProductService productService;

  @Transactional
  public ImageResponseDto uploadImage(MultipartFile multipartFile, UUID productId)
      throws IOException {

    checkFileIsSelected(multipartFile);
    checkContentType(multipartFile);
    checkFileSize(multipartFile, maxFileSize);

    Path directoryPath = Path.of(folderPath + productId);
    Path imagePath = createFileNamePath(multipartFile, directoryPath);
    Product product = getProduct(productId);

    Image image = createImageData(multipartFile, product, imagePath);
    setProductImage(product, image);
    saveImagetoFileSystem(multipartFile, directoryPath, imagePath);
    logger.info("Uploaded image for product id - {" + productId + "}. Path: {" + imagePath + "}");
    return imageDataMapper.toImageResponse(image);
  }

  @Transactional
  public byte[] downloadImage(UUID productId) throws IOException {
    Product product = getProduct(productId);
    checkForAttachedPicture(product);
    logger.debug(
        "Downloaded image for product id - {"
            + productId
            + "}. File path: {"
            + product.getImage().getFilePath()
            + "}");
    return Files.readAllBytes(new File(product.getImage().getFilePath()).toPath());
  }

  @Transactional
  public void deleteImage(UUID productId) throws IOException {
    logger.info("Deleted image for product id - {" + productId + "}");
    removeImage(getProduct(productId));
  }

  private void saveImagetoFileSystem(
      MultipartFile multipartFile, Path directoryPath, Path imagePath) throws IOException {
    createDirectoryIfNotExists(directoryPath);
    Files.copy(multipartFile.getInputStream(), imagePath);
  }

  private Image createImageData(MultipartFile file, Product product, Path imagePath) {
    return imageRepository.save(
        Image.builder()
            .type(file.getContentType())
            .filePath(imagePath.toString())
            .product(product)
            .build());
  }

  private void createDirectoryIfNotExists(Path directoryPath) throws IOException {
    if (Files.notExists(directoryPath)) {
      logger.debug("Creating directory: {" + directoryPath + "}");
      Files.createDirectories(directoryPath);
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
    FileSystemUtils.deleteRecursively(getImageFolderPath(product));
    product.setImage(null);
    imageRepository.delete(image);
  }

  private static Path getImageFolderPath(Product product) {
    return Path.of(
        product
            .getImage()
            .getFilePath()
            .substring(0, product.getImage().getFilePath().indexOf(FILE_NAME)));
  }

  private static void checkForAttachedPicture(Product product) {
    if (product.getImage() == null) {
      logger.error("No image attached for product id - {" + product.getId() + "}");
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
      logger.error("Unsupported content type for file: {" + file.getContentType() + "}");
      throw new MultipartFileContentTypeException();
    }
  }

  private static void checkFileIsSelected(MultipartFile file) {
    if (file.isEmpty()) {
      logger.error("No file selected.");
      throw new MultipartFileNotSelectedException();
    }
  }

  private static void checkFileSize(MultipartFile multipartFile, Integer fileSize) {
    if (multipartFile.getSize() > (long) fileSize * MB) {
      logger.error(
          "File size exceeds the allowed limit. File size: {"
              + multipartFile.getSize()
              + "} bytes, Allowed size: {"
              + fileSize
              + "} MB");
      throw new MultipartFileSizeException(fileSize);
    }
  }

  private static Path createFileNamePath(MultipartFile multipartFile, Path directory) {
    StringBuilder sb = new StringBuilder();
    sb.append(directory).append("/");

    switch (Objects.requireNonNull(multipartFile.getContentType())) {
      case "image/png" -> sb.append(FILE_NAME + ".png");
      case "image/jpg" -> sb.append(FILE_NAME + ".jpg");
      case "image/jpeg" -> sb.append(FILE_NAME + ".jpeg");
      default -> {
        logger.error("Unsupported content type: {" + multipartFile.getContentType() + "}");
        throw new MultipartFileContentTypeException();
      }
    }
    return Path.of(sb.toString());
  }
}
