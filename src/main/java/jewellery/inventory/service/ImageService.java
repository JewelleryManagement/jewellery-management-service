package jewellery.inventory.service;

import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.exception.duplicate.DuplicateFileException;
import jewellery.inventory.exception.not_found.ImageNotFoundException;
import jewellery.inventory.mapper.ImageDataMapper;
import jewellery.inventory.model.Image;
import jewellery.inventory.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ImageService {

  private static final String FOLDER_PATH = "C:/Windows/Temp/Jms/Images/";

  private final ImageRepository imageRepository;
  private final ImageDataMapper imageDataMapper;

  @Transactional
  public ImageResponseDto uploadImage(MultipartFile file) throws IOException {
    String filePath = FOLDER_PATH + file.getOriginalFilename();
    createDirectoryIfNotExists();
    checkForExistedImage(file);
    file.transferTo(new File(filePath));
    return imageDataMapper.toImageResponse(createImageData(file, filePath));
  }

  @Transactional
  public byte[] downloadImage(String name) throws IOException {
    Image fileData = getImage(name);
    return Files.readAllBytes(new File(fileData.getFilePath()).toPath());
  }

  @Transactional
  public void deleteImage(String name) throws IOException {
    Image fileData = getImage(name);
    Path path = Paths.get(fileData.getFilePath());
    Files.delete(path);
    imageRepository.delete(fileData);
  }

  private Image createImageData(MultipartFile file, String filePath) {
    return imageRepository.save(
            Image.builder()
                    .name(file.getOriginalFilename())
                    .type(file.getContentType())
                    .filePath(filePath)
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
}
