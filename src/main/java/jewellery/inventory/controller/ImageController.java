package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jewellery.inventory.dto.request.ImageRequestDto;
import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/picture")
@RequiredArgsConstructor
public class ImageController {

  private final ImageService imageService;

  @Operation(summary = "Upload new image in file system and attach to product")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ImageResponseDto uploadImage(
      @Valid ImageRequestDto file,
      @PathVariable("productId") @Valid UUID productId)
      throws IOException {
    return imageService.uploadImage(file, productId);
  }

  @Operation(summary = "Get image of product")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(produces = "image/png")
  public byte[] getImage(@PathVariable("productId") @Valid UUID productId) throws IOException {
    return imageService.downloadImage(productId);
  }

  @Operation(summary = "Delete image from file system and detach from product")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void deleteImage(@PathVariable("productId") @Valid UUID productId) throws IOException {
    imageService.deleteImage(productId);
  }
}
