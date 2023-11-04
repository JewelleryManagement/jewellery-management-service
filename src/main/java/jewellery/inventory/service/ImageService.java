package jewellery.inventory.service;

import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.exception.duplicate.DuplicateFileException;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

  private static final String FOLDER_PATH = "C:/Windows/Temp/Jms/Images/";

  private final ImageRepository imageRepository;
  private final ImageDataMapper imageDataMapper;
  private final ProductRepository productRepository;

  @Transactional
  public ImageResponseDto uploadImage(MultipartFile file, UUID productId) throws IOException {

    if (file.isEmpty()) {
      throw new ImageNotFoundException();
    }

    String filePath = FOLDER_PATH + file.getOriginalFilename();
    createDirectoryIfNotExists();
    checkForExistedImage(file);
    file.transferTo(new File(filePath));

    Product product = getProduct(productId);
    Image image = createImageData(file, filePath, product);
    setProductImage(product, image);

    return imageDataMapper.toImageResponse(image);
  }

  @Transactional
  public byte[] downloadImage(String name, UUID productId) throws IOException {
    Image fileData = getImage(name);
    //TODO: <<<
//    Product product = getProduct(productId);
//    if (!product.getImages().contains(fileData)) {
//      throw new ImageNotFoundException(fileData.getName());
//    }
    // >>>
    return Files.readAllBytes(new File(fileData.getFilePath()).toPath());
  }

  @Transactional
  public void deleteImage(String name, UUID productId) throws IOException {

    Image fileData = getImage(name);
    removeImageFromProduct(productId);

    Path path = Paths.get(fileData.getFilePath());
    Files.delete(path);
    imageRepository.delete(fileData);
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
    return productRepository.findById(productId).orElseThrow(
            () -> new ProductNotFoundException(productId));
  }

  private void removeImageFromProduct(UUID productId) {
    Product product = getProduct(productId);
    product.setImage(null);
  }

  private void setProductImage(Product product, Image image) {
    if (product.getImage() != null) {
      imageRepository.delete(product.getImage());
    }
    product.setImage(image);
  }
}
