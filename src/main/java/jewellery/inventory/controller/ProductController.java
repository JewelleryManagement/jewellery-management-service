package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductsInOrganizationResponseDto;
import jewellery.inventory.service.ImageService;
import jewellery.inventory.service.ProductService;
import jewellery.inventory.utils.NotUsedYet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final ImageService imageService;

  @Operation(summary = "Get products owned by user")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/by-owner/{ownerId}")
  public List<ProductResponseDto> getProductsByOwner(@PathVariable("ownerId") UUID ownerId) {
    return productService.getByOwner(ownerId);
  }

  @Operation(summary = "Get a single product")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@orgAuth.hasPermissionForProduct(#id, 'ORGANIZATION_PRODUCT_READ')")
  @GetMapping("/{id}")
  public ProductResponseDto getProduct(@PathVariable("id") UUID id) {
    return productService.getProductResponse(id);
  }

  @Operation(summary = "Upload new image in file system and attach to product")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("@orgAuth.hasPermissionForProduct(#productId, 'ORGANIZATION_PRODUCT_CREATE')")
  @PostMapping(value = "/{productId}/picture")
  public ImageResponseDto uploadImage(
      @PathVariable("productId") @Valid UUID productId,
      @RequestParam("image") @Valid MultipartFile image)
      throws IOException {
    return imageService.uploadImage(image, productId);
  }

  @Operation(summary = "Get image of product")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@orgAuth.hasPermissionForProduct(#productId, 'ORGANIZATION_PRODUCT_READ')")
  @GetMapping(value = "/{productId}/picture", produces = "image/png")
  public byte[] getImage(@PathVariable("productId") @Valid UUID productId) throws IOException {
    return imageService.downloadImage(productId);
  }

  @NotUsedYet(reason = "Pending frontend implementation")
  @Operation(summary = "Delete image from file system and detach from product")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("@orgAuth.hasPermissionForProduct(#productId, 'ORGANIZATION_PRODUCT_UPDATE')")
  @DeleteMapping("/{productId}/picture")
  public void deleteImage(@PathVariable("productId") @Valid UUID productId) throws IOException {
    imageService.deleteImage(productId);
  }

  @Operation(summary = "Create a new product")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("@orgAuth.hasPermission(#productRequestDto.ownerId, 'ORGANIZATION_PRODUCT_CREATE')")
  @PostMapping
  public ProductsInOrganizationResponseDto createProduct(
      @RequestBody @Valid ProductRequestDto productRequestDto) {
    return productService.createProductInOrganization(productRequestDto);
  }

  @Operation(summary = "Delete a new product in organization")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("@orgAuth.hasPermissionForProduct(#productId, 'ORGANIZATION_PRODUCT_DELETE')")
  @DeleteMapping("/{productId}")
  public void deleteProduct(@PathVariable("productId") UUID productId) {
    productService.deleteProductInOrganization(productId);
  }

  @Operation(summary = "Update a product in organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@orgAuth.hasPermissionForProduct(#productId, 'ORGANIZATION_PRODUCT_UPDATE')")
  @PutMapping("/{productId}")
  public ProductsInOrganizationResponseDto updateProduct(
      @PathVariable("productId") UUID productId,
      @Valid @RequestBody ProductRequestDto productRequestDto) {
    return productService.updateProduct(productId, productRequestDto);
  }

  @Operation(summary = "Transfer a product to other organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(
      "@orgAuth.hasPermissionForProduct(#productId, 'ORGANIZATION_PRODUCT_TRANSFER')&& "
          + "@orgAuth.hasPermission(#recipientId, 'ORGANIZATION_PRODUCT_TRANSFER')")
  @PutMapping("/{productId}/transfer/{recipientId}")
  public ProductsInOrganizationResponseDto transferProduct(
      @PathVariable("productId") UUID productId, @PathVariable("recipientId") UUID recipientId) {
    return productService.transferProduct(productId, recipientId);
  }

  @Operation(summary = "Get all products for resource")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/resource/{resourceId}")
  public List<ProductResponseDto> getAllProductsByResource(
      @PathVariable("resourceId") UUID resourceId) {
    return productService.getAllProductsByResource(resourceId);
  }
}
