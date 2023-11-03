package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ImageController {

  private final ImageService imageService;

  @Operation(summary = "Upload new image in file system")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{productId}/picture")
  public ImageResponseDto uploadImage(
      @RequestParam("image") @Valid MultipartFile file,
      @PathVariable("productId") @Valid UUID productId)
      throws IOException {
    return imageService.uploadImage(file, productId);
  }

  @Operation(summary = "Get image")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{fileName}", produces = "image/png")
  public byte[] getImage(@PathVariable @Valid String fileName) throws IOException {
    return imageService.downloadImage(fileName);
  }

  @Operation(summary = "Delete image from file system")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{productId}/picture/{fileName}")
  public void deleteImage(@PathVariable @Valid String fileName,
                          @PathVariable @Valid UUID productId) throws IOException {
    imageService.deleteImage(fileName, productId);
  }
}
