package jewellery.inventory.controller;

import jakarta.validation.Valid;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "${cors.origins}")
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ProductResponseDto createProduct(@RequestBody ProductRequestDto productRequestDto) {
        return productService.createProduct(productRequestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/all")
    public List<ProductResponseDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public ProductResponseDto getProduct(@PathVariable("id") UUID id) {
        return productService.getProduct(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable("id") UUID id) {
        productService.deleteProduct(id);
    }
}
