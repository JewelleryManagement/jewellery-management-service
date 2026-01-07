package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.*;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.repository.*;
import jewellery.inventory.utils.NotUsedYet;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
  private static final Logger logger = LogManager.getLogger(ProductService.class);
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  public List<ProductResponseDto> getByOwner(UUID ownerId) {
    List<Product> products = productRepository.findAllByOwnerId(ownerId);
    logger.info("Get product by owner with ID: {}", ownerId);
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  public Product getProduct(UUID id) {
    logger.info("Get product by ID: {}", id);
    return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
  }

  @NotUsedYet(reason = "Pending frontend implementation")
  public ProductResponseDto getProductResponse(UUID id) {
    logger.info("Get productResponse by ID: {}", id);
    return productMapper.mapToProductResponseDto(getProduct(id));
  }
}
