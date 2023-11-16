package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

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
    return productService.getProductWithoutResourcesAndProduct(id);
  }

  @Operation(summary = "Transfer a product")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{productId}/transfer/{recipientId}")
  public ProductResponseDto transferProduct(
      @PathVariable("productId") UUID productId, @PathVariable("recipientId") UUID recipientId) {
    return productService.transferProduct(productId, recipientId);
  }

  @Operation(summary = "Delete a product by Id")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteProduct(@PathVariable("id") UUID id) {
    productService.deleteProduct(id);
  }
}
