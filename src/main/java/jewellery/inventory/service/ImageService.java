package jewellery.inventory.service;

import jewellery.inventory.dto.request.ImageRequestDto;
import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.exception.image.MultipartFileContentTypeException;
import jewellery.inventory.exception.duplicate.DuplicateFileException;
import jewellery.inventory.exception.image.MultipartFileNotSelectedException;
import jewellery.inventory.exception.not_found.ImageNotFoundException;
import jewellery.inventory.exception.not_found.ProductNotFoundException;
import jewellery.inventory.mapper.ImageDataMapper;
import jewellery.inventory.model.Image;
import jewellery.inventory.model.Product;
import jewellery.inventory.repository.ImageRepository;
import jewellery.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

  private static final String FOLDER_PATH = "C:/Windows/Temp/Jms/Images/";

  private final ImageRepository imageRepository;
  private final ImageDataMapper imageDataMapper;
  private final ProductRepository productRepository;

  @Transactional
  public ImageResponseDto uploadImage(ImageRequestDto imageRequestDto, UUID productId) throws IOException {

    checkFileIsSelected(imageRequestDto.getImage());
    checkContentType(imageRequestDto.getImage());

    String filePath = FOLDER_PATH + imageRequestDto.getImage().getOriginalFilename();
    createDirectoryIfNotExists();
    checkForExistedImage(imageRequestDto.getImage());
    imageRequestDto.getImage().transferTo(new File(filePath));

    Product product = getProduct(productId);
    Image image = createImageData(imageRequestDto.getImage(), filePath, product);
    setProductImage(product, image);

    return imageDataMapper.toImageResponse(image);
  }

  @Transactional
  public byte[] downloadImage(UUID productId) throws IOException {
    Product product = getProduct(productId);
    checkForAttachedPicture(product);
    Image fileData = getImage(product.getImage().getName());
    return Files.readAllBytes(new File(fileData.getFilePath()).toPath());
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

  private void checkForExistedImage(MultipartFile file) {
    Image image = imageRepository.findByName(file.getOriginalFilename()).orElse(null);
    if (image != null) {
      throw new DuplicateFileException(file.getOriginalFilename());
    }
  }

  private void createDirectoryIfNotExists() {
    File path = new File(FOLDER_PATH);
    if (imageRepository.count() == 0) {
      path.mkdirs();
    }
  }

  private Image getImage(String name) {
    return imageRepository.findByName(name).orElseThrow(() -> new ImageNotFoundException(name));
  }

  private Product getProduct(UUID productId) {
    return productRepository
        .findById(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));
  }

  private void setProductImage(Product product, Image image) throws IOException {
    if (product.getImage() != null) {
      removeImage(product);
    }
    product.setImage(image);
  }

  private void removeImage(Product product) throws IOException {
    checkForAttachedPicture(product);
    Image fileData = getImage(product.getImage().getName());
    Files.deleteIfExists(Paths.get(fileData.getFilePath()));
    product.setImage(null);
    imageRepository.delete(fileData);
  }

  private static void checkForAttachedPicture(Product product) {
    if (product.getImage() == null) {
      throw new ImageNotFoundException();
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
}
