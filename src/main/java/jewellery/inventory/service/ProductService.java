package jewellery.inventory.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
import jewellery.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final ResourceInUserRepository resourceInUserRepository;
  private final ResourceInUserService resourceInUserService;
  private final ProductMapper productMapper;

  @Transactional
  public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
    User owner = getUser(productRequestDto.getOwnerId());
    Product product = createProductWithoutResourcesAndProducts(productRequestDto, owner);
    addProductsContentToProduct(productRequestDto, product);
    addResourcesToProduct(productRequestDto, owner, product);
    return productMapper.mapToProductResponseDto(product);
  }

  private User getUser(UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
  }

  private Product createProductWithoutResourcesAndProducts(
      ProductRequestDto productRequestDto, User user) {
    Product product = getProduct(productRequestDto, user);
    productRepository.save(product);
    return product;
  }

  private static Product getProduct(ProductRequestDto productRequestDto, User user) {
    Product product = new Product();
    product.setOwner(user);
    product.setAuthors(productRequestDto.getAuthors());
    product.setSold(false);
    product.setDescription(productRequestDto.getDescription());
    product.setSalePrice(productRequestDto.getSalePrice());
    return product;
  }

  private void addProductsContentToProduct(ProductRequestDto productRequestDto, Product product) {
    if (productRequestDto.getProductsContent() == null) {
      product.setProductsContent(null);
    } else {
      product.setProductsContent(
          getProductsInProduct(productRequestDto.getProductsContent(), product));
      productRepository.save(product);
    }
  }

  private void addResourcesToProduct(
      ProductRequestDto productRequestDto, User user, Product product) {
    List<ResourceInProduct> resourcesInProducts =
        transferResourcesQuantitiesFromUserToProduct(
            user, productRequestDto.getResourcesContent(), product);
    product.setResourcesContent(resourcesInProducts);
  }

  private List<ResourceInProduct> transferResourcesQuantitiesFromUserToProduct(
      User owner, List<ResourceQuantityRequestDto> incomingResourceInProductList, Product product) {

    return incomingResourceInProductList.stream()
        .map(
            incomingResourceInProduct ->
                transferSingleResourceQuantityFromUserToProduct(
                    owner, incomingResourceInProduct, product))
        .toList();
  }

  private ResourceInProduct transferSingleResourceQuantityFromUserToProduct(
      User owner, ResourceQuantityRequestDto incomingResourceInProduct, Product product) {
    ResourceInUser resourceInUser = getResourceInUser(owner, incomingResourceInProduct.getId());
    resourceInUserService.removeQuantityFromResource(
        owner.getId(),
        resourceInUser.getResource().getId(),
        incomingResourceInProduct.getQuantity());
    return createResourceInProduct(
        incomingResourceInProduct, resourceInUser.getResource(), product);
  }

  private ResourceInUser getResourceInUser(User owner, UUID resourceId) {
    return resourceInUserRepository
        .findByResourceIdAndOwnerId(resourceId, owner.getId())
        .orElseThrow(() -> new ResourceInUserNotFoundException(resourceId, owner.getId()));
  }

  private ResourceInProduct createResourceInProduct(
      ResourceQuantityRequestDto incomingResourceInProduct, Resource resource, Product product) {
    ResourceInProduct resourceInProduct = new ResourceInProduct();
    resourceInProduct.setResource(resource);
    resourceInProduct.setQuantity(incomingResourceInProduct.getQuantity());
    resourceInProduct.setProduct(product);
    return resourceInProduct;
  }

  public List<ProductResponseDto> getAllProducts() {
    List<Product> products = productRepository.findAll();
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  public ProductResponseDto getProduct(UUID id) {
    Product product =
        productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

    return productMapper.mapToProductResponseDto(product);
  }

  public void deleteProduct(UUID id) {

    Product product =
        productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

    throwExceptionIfProductIsSold(id, product);
    throwExceptionIfProductIsPartOfAnotherProduct(id, product);

    moveResourceInProductToResourceInUser(product);
    disassembleProductContent(product);

    productRepository.deleteById(id);
  }

  private void throwExceptionIfProductIsPartOfAnotherProduct(UUID id, Product product) {
    if (product.getContentOf() != null) {
      throw new ProductIsContentException(id);
    }
  }

  private void throwExceptionIfProductIsSold(UUID id, Product product) {
    if (product.isSold()) {
      throw new ProductIsSoldException(id);
    }
  }

  private void disassembleProductContent(Product product) {
    if (product.getProductsContent() != null) {
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

  private void moveResourceInProductToResourceInUser(Product product) {
    List<ResourceInProduct> resourcesInProduct = product.getResourcesContent();
    User owner = product.getOwner();

    resourcesInProduct.forEach(
        resourceInProduct ->
            resourceInUserService.addResourceToUser(
                getResourceInUserRequest(owner, resourceInProduct)));
  }

  private ResourceInUserRequestDto getResourceInUserRequest(
      User owner, ResourceInProduct resourceInProduct) {

    return ResourceInUserRequestDto.builder()
        .userId(owner.getId())
        .resourceId(resourceInProduct.getResource().getId())
        .quantity(resourceInProduct.getQuantity())
        .build();
  }

  private List<Product> getProductsInProduct(
      List<UUID> productsIdInRequest, Product parentProduct) {
    List<Product> products = new ArrayList<>();
    if (productsIdInRequest != null) {
      productsIdInRequest.forEach(
          productId -> {
            Product product =
                productRepository
                    .findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            product.setContentOf(parentProduct);
            products.add(product);
          });
    }

    return products;
  }
}
