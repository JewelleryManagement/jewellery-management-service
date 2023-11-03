package jewellery.inventory.service;

import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.exception.duplicate.DuplicateFileException;
import jewellery.inventory.exception.not_found.ImageNotFoundException;
import jewellery.inventory.mapper.ImageDataMapper;
import jewellery.inventory.model.Image;
import jewellery.inventory.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class ImageService {

  private static final String FOLDER_PATH = "C:/images/";

  private final ImageRepository imageRepository;
  private final ImageDataMapper imageDataMapper;


  public ImageResponseDto uploadImageToFileSystem(MultipartFile file) throws IOException {

    String filePath = FOLDER_PATH + file.getOriginalFilename();

    checkForExistedImage(file);
    Image imageData = getImage(file, filePath);
    file.transferTo(new File(filePath));

    return imageDataMapper.toImageResponse(imageData);
  }

  public byte[] downloadImageFormFileSystem(String name) throws IOException {
    Image fileData =
        imageRepository.findByName(name).orElseThrow(() -> new ImageNotFoundException(name));

    return Files.readAllBytes(new File(fileData.getPath()).toPath());
  }

  private Image getImage(MultipartFile file, String filePath) {
    Image imageData = imageRepository.save(
            Image.builder()
                    .name(file.getOriginalFilename())
                    .type(file.getContentType())
                    .path(filePath)
                    .build());
    return imageData;
  }

  private void checkForExistedImage(MultipartFile file) {
    Image image = imageRepository.findByName(file.getOriginalFilename()).orElse(null);

    if (image != null) {
      throw new DuplicateFileException(file.getOriginalFilename());
    }
  }
}
