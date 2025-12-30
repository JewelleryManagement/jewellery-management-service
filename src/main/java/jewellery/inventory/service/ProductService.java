package jewellery.inventory.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.*;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
import jewellery.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(ProductService.class);
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final ProductMapper productMapper;

  public ProductReturnResponseDto getProductReturnResponseDto(
      SaleResponseDto sale, Product product) {
    return productMapper.mapToProductReturnResponseDto(sale, product);
  }

  public BigDecimal getProductSalePrice(Product product) {
    return ProductMapper.calculateTotalPrice(product);
  }

  public List<ProductResponseDto> getProductsResponse(List<Product> products) {
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  public List<ProductResponseDto> getByOwner(UUID ownerId) {
    List<Product> products = productRepository.findAllByOwnerId(ownerId);
    logger.info("Get product by owner with ID: {}", ownerId);
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  public Product getProduct(UUID id) {
    logger.info("Get product by ID: {}", id);
    return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
  }

  public ProductResponseDto getProductResponse(UUID id) {
    logger.info("Get productResponse by ID: {}", id);
    return productMapper.mapToProductResponseDto(getProduct(id));
  }

  public Product saveProduct(Product product) {
    return productRepository.save(product);
  }

  public void updateProductOwnerAndSale(Product product, User newOwner, Sale sale) {
    updateProductOwnerRecursively(product, newOwner);
    if (sale == null) {
      product.setPartOfSale(null);
    } else {
      sale.getProducts()
          .forEach(
              productPriceDiscount -> {
                if (productPriceDiscount.getProduct().equals(product)) {
                  product.setPartOfSale(productPriceDiscount);
                }
              });
    }
    logger.debug(
        "Updated product owner and sale for product with ID: {}. New owner with ID: {}, Sale with ID: {}",
        product.getId(),
        product.getOwner().getId(),
        product.getPartOfSale() != null ? product.getPartOfSale().getId() : null);
    productRepository.save(product);
  }

  private void updateProductOwnerRecursively(Product product, User newOwner) {
    product.setOwner(newOwner);
    logger.debug(
        "Updated owner for product with ID: {}. New owner with ID: {}",
        product.getId(),
        newOwner.getId());
    if (product.getProductsContent() != null) {
      List<Product> subProducts = product.getProductsContent();
      for (Product subProduct : subProducts) {
        updateProductOwnerRecursively(subProduct, newOwner);
      }
    }
  }

  public void deleteProductById(UUID productId) {
    productRepository.deleteById(productId);
  }

  public void throwExceptionIfProductIsPartOfAnotherProduct(UUID id, Product product) {
    if (product.getContentOf() != null) {
      throw new ProductIsContentException(id);
    }
  }

  public void throwExceptionIfProductIsSold(Product product) {
    if (product.getPartOfSale() != null) {
      throw new ProductIsSoldException(product.getId());
    }
  }

  public void disassembleProductContent(Product product) {
    if (product.getProductsContent() != null) {
      logger.debug("Disassembling product content for product with ID: {}", product.getId());

      product
          .getProductsContent()
          .forEach(
              content -> {
                content.setContentOf(null);
                productRepository.save(content);
              });

      product.setProductsContent(new ArrayList<>());
      productRepository.save(product);
    }
  }

  public void throwExceptionIfProductIsPartOfItself(Product product, UUID parentId) {
    if (product.getId().equals(parentId)) {
      throw new ProductPartOfItselfException();
    }
  }

  public List<User> getAuthors(ProductRequestDto productRequestDto) {
    logger.debug("Getting authors for product.");
    List<UUID> authorsIds = productRequestDto.getAuthors();
    List<User> authors = new ArrayList<>();
    authorsIds.forEach(
        id -> {
          logger.debug("Processing author with ID: {}", id);
          User author =
              userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
          authors.add(author);
          logger.debug("Author with ID {} added to the list.", id);
        });
    return authors;
  }

  @Override
  public Object fetchEntity(Object... ids) {
    Product product = productRepository.findById((UUID) ids[0]).orElse(null);
    if (product == null) {
      return null;
    }
    return productMapper.mapToProductResponseDto(product);
  }
}
