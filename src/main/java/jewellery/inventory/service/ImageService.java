package jewellery.inventory.service;

import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.exception.not_found.ImageNotFoundException;
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

  private final ImageRepository imageRepository;
  private static final String FOLDER_PATH = "src/main/resources/images/";

  public ImageResponseDto uploadImageToFileSystem(MultipartFile file) throws IOException {

    String filePath = FOLDER_PATH + file.getOriginalFilename();

    Image imageData = imageRepository.save(
        Image.builder()
            .name(file.getOriginalFilename())
            .type(file.getContentType())
            .imagePath(filePath)
            .build());

    file.transferTo(new File(filePath));

    return ImageResponseDto.builder()
        .name(imageData.getName())
        .type(imageData.getType())
        .filePath(imageData.getImagePath())
        .build();
  }

  public byte[] downloadImageFormFileSystem(String name) throws IOException {
    Image fileData =
        imageRepository.findByName(name).orElseThrow(() -> new ImageNotFoundException(name));

    return Files.readAllBytes(new File(fileData.getImagePath()).toPath());
  }
}
