package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.service.ImageService;
import jewellery.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final ImageService imageService;

  @Operation(summary = "Create a new product")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ProductResponseDto createProduct(@RequestBody @Valid ProductRequestDto productRequestDto) {
    return productService.createProduct(productRequestDto);
  }

  @Operation(summary = "Get all products")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public List<ProductResponseDto> getAllProducts() {
    return productService.getAllProducts();
  }

  @Operation(summary = "Get products owned by user")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/by-owner/{ownerId}")
  public List<ProductResponseDto> getProductsByOwner(@PathVariable("ownerId") UUID ownerId) {
    return productService.getByOwner(ownerId);
  }

  @Operation(summary = "Get a single product")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}")
  public ProductResponseDto getProduct(@PathVariable("id") UUID id) {
    return productService.getProductResponse(id);
  }

  @Operation(summary = "Transfer a product")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{productId}/transfer/{recipientId}")
  public ProductResponseDto transferProduct(
      @PathVariable("productId") UUID productId, @PathVariable("recipientId") UUID recipientId) {
    return productService.transferProduct(recipientId, productId);
  }

  @Operation(summary = "Delete a product by Id")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteProduct(@PathVariable("id") UUID id) throws IOException {
    productService.deleteProduct(id);
  }

  @Operation(summary = "Upload new image in file system and attach to product")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{productId}/picture")
  public ImageResponseDto uploadImage(
          @PathVariable("productId") @Valid UUID productId,
          @RequestParam("image") @Valid MultipartFile image)
          throws IOException {
    return imageService.uploadImage(image, productId);
  }

  @Operation(summary = "Get image of product")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{productId}/picture", produces = "image/png")
  public byte[] getImage(@PathVariable("productId") @Valid UUID productId) throws IOException {
    return imageService.downloadImage(productId);
  }

  @Operation(summary = "Delete image from file system and detach from product")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{productId}/picture")
  public void deleteImage(@PathVariable("productId") @Valid UUID productId) throws IOException {
    imageService.deleteImage(productId);
  }
}
