package jewellery.inventory.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.MinimalResourceQuantityException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService implements EntityFetcher {

  private static final double MINIMAL_RESOURCE_QUANTITY = 0.01;

  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final ResourceInUserRepository resourceInUserRepository;
  private final ResourceInProductRepository resourceInProductRepository;
  private final ImageService imageService;
  private final ResourceInUserService resourceInUserService;
  private final ProductMapper productMapper;

  @Transactional
  @LogUpdateEvent(eventType = EventType.PRODUCT_CREATE)
  public ProductResponseDto updateProduct(UUID id, ProductRequestDto productUpdateRequestDto) {
    Product product = getProduct(id);
    throwExceptionIfProductIsSold(product);
    moveResourceInProductToResourceInUser(product);

    persistProductWithoutResourcesAndProducts(
        productUpdateRequestDto, getUser(productUpdateRequestDto.getOwnerId()));
    addProductsContentToProduct(productUpdateRequestDto, product);
    addResourcesToProduct(
        productUpdateRequestDto, getUser(productUpdateRequestDto.getOwnerId()), product);
    return productMapper.mapToProductResponseDto(product);
  }

  @Transactional
  @LogCreateEvent(eventType = EventType.PRODUCT_CREATE)
  public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
    User owner = getUser(productRequestDto.getOwnerId());
    Product product = persistProductWithoutResourcesAndProducts(productRequestDto, owner);
    addProductsContentToProduct(productRequestDto, product);
    addResourcesToProduct(productRequestDto, owner, product);
    return productMapper.mapToProductResponseDto(product);
  }

  public ProductReturnResponseDto getProductReturnResponseDto(
      SaleResponseDto sale, Product product) {
    return productMapper.mapToProductReturnResponseDto(sale, product);
  }

  public List<ProductResponseDto> getAllProducts() {
    List<Product> products = productRepository.findAll();
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  public List<ProductResponseDto> getByOwner(UUID ownerId) {
    List<Product> products = productRepository.findAllByOwnerId(ownerId);
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  public Product getProduct(UUID id) {
    return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
  }

  public ProductResponseDto getProductResponse(UUID id) {
    return productMapper.mapToProductResponseDto(getProduct(id));
  }

  public void updateProductOwnerAndSale(Product product, User newOwner, Sale sale) {
    updateProductOwnerRecursively(product, newOwner);
    product.setPartOfSale(sale);
    productRepository.save(product);
  }

  private void updateProductOwnerRecursively(Product product, User newOwner) {
    product.setOwner(newOwner);
    if (product.getProductsContent() != null) {
      List<Product> subProducts = product.getProductsContent();
      for (Product subProduct : subProducts) {
        updateProductOwnerRecursively(subProduct, newOwner);
      }
    }
  }

  @Transactional
  @LogDeleteEvent(eventType = EventType.PRODUCT_DISASSEMBLY)
  public void deleteProduct(UUID id) throws IOException {

    Product product = getProduct(id);

    throwExceptionIfProductIsSold(product);
    throwExceptionIfProductIsPartOfAnotherProduct(id, product);

    moveResourceInProductToResourceInUser(product);
    disassembleProductContent(product);
    deleteImageWhenAttached(id, product);

    productRepository.deleteById(id);
  }

  @LogUpdateEvent(eventType = EventType.PRODUCT_TRANSFER)
  public ProductResponseDto transferProduct(UUID productId, UUID recipientId) {
    Product productForChangeOwner = getProductForTransfer(recipientId, productId);
    updateProductOwnerRecursively(productForChangeOwner, getUser(recipientId));
    productRepository.save(productForChangeOwner);
    return productMapper.mapToProductResponseDto(productForChangeOwner);
  }

  private void deleteImageWhenAttached(UUID id, Product product) throws IOException {
    if (product.getImage() != null) {
      imageService.deleteImage(id);
    }
  }

  private void throwExceptionIfProductIsPartOfAnotherProduct(UUID id, Product product) {
    if (product.getContentOf() != null) {
      throw new ProductIsContentException(id);
    }
  }

  private void throwExceptionIfProductOwnerEqualsRecipient(Product product, UUID recipientId) {
    if (product.getOwner().getId().equals(recipientId)) {
      throw new ProductOwnerEqualsRecipientException(recipientId);
    }
  }

  private void throwExceptionIfProductIsSold(Product product) {
    if (product.getPartOfSale() != null) {
      throw new ProductIsSoldException(product.getId());
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
    moveQuantityFromResourcesInProductToResourcesInUser(resourcesInProduct, owner);
  }

  private void moveQuantityFromResourcesInProductToResourcesInUser(
      List<ResourceInProduct> resourcesInProduct, User owner) {
    resourcesInProduct.forEach(
        resourceInProduct -> {
          resourceInUserService.addResourceToUserNoLog(
              getResourceInUserRequest(owner, resourceInProduct));
          resourceInProduct.setQuantity(0);
        });
  }

  private ResourceInUserRequestDto getResourceInUserRequest(
      User owner, ResourceInProduct resourceInProduct) {

    return ResourceInUserRequestDto.builder()
        .userId(owner.getId())
        .resourceId(resourceInProduct.getResource().getId())
        .quantity(resourceInProduct.getQuantity())
        .build();
  }

  private ResourceInUser getResourceInUser(User owner, UUID resourceId) {
    return resourceInUserRepository
        .findByResourceIdAndOwnerId(resourceId, owner.getId())
        .orElseThrow(() -> new ResourceInUserNotFoundException(resourceId, owner.getId()));
  }

  private Product getProductForTransfer(UUID recipientId, UUID productId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    validateProductForChangeOwner(recipientId, product);
    return product;
  }

  private void validateProductForChangeOwner(UUID recipientId, Product productForChangeOwner) {
    throwExceptionIfProductIsPartOfAnotherProduct(
        productForChangeOwner.getId(), productForChangeOwner);
    throwExceptionIfProductIsSold(productForChangeOwner);
    throwExceptionIfProductOwnerEqualsRecipient(productForChangeOwner, recipientId);
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
            if (product.getOwner().getId().equals(parentProduct.getOwner().getId())) {
              product.setContentOf(parentProduct);
              products.add(product);
            } else {
              throw new UserNotOwnerException(parentProduct.getOwner().getId(), product.getId());
            }
          });
    }

    return products;
  }

  private User getUser(UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
  }

  private Product persistProductWithoutResourcesAndProducts(
      ProductRequestDto productRequestDto, User user) {
    Product product = getProductWithoutResourcesAndProduct(productRequestDto, user);
    productRepository.save(product);
    return product;
  }

  private Product getProductWithoutResourcesAndProduct(
      ProductRequestDto productRequestDto, User user) {
    Product product = new Product();
    product.setOwner(user);
    product.setAuthors(getAuthors(productRequestDto));
    product.setPartOfSale(null);
    product.setDescription(productRequestDto.getDescription());
    product.setSalePrice(productRequestDto.getSalePrice());
    product.setProductionNumber(productRequestDto.getProductionNumber());
    product.setCatalogNumber(productRequestDto.getCatalogNumber());
    product.setProductsContent(new ArrayList<>());
    product.setResourcesContent(new ArrayList<>());
    return product;
  }

  private List<User> getAuthors(ProductRequestDto productRequestDto) {
    List<UUID> authorsIds = productRequestDto.getAuthors();
    List<User> authors = new ArrayList<>();
    authorsIds.forEach(
        id -> {
          User author =
              userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
          authors.add(author);
        });
    return authors;
  }

  private void addProductsContentToProduct(ProductRequestDto productRequestDto, Product product) {
    if (productRequestDto.getProductsContent() != null) {
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
    throwWhenIncomingResourceQuantityIsLessThanMinimum(incomingResourceInProduct);
    resourceInUserService.removeQuantityFromResourceNoLog(
        owner.getId(),
        resourceInUser.getResource().getId(),
        incomingResourceInProduct.getQuantity());
    ResourceInProduct resourceInProduct = getResourceInProduct(incomingResourceInProduct, product);
    if (resourceInProduct != null) {
      resourceInProduct.setQuantity(incomingResourceInProduct.getQuantity());
      return resourceInProduct;
    }
    return createResourceInProduct(
        incomingResourceInProduct, resourceInUser.getResource(), product);
  }

  private static void throwWhenIncomingResourceQuantityIsLessThanMinimum(
      ResourceQuantityRequestDto incomingResourceInProduct) {
    if (incomingResourceInProduct.getQuantity() < MINIMAL_RESOURCE_QUANTITY) {
      throw new MinimalResourceQuantityException();
    }
  }

  private ResourceInProduct getResourceInProduct(
      ResourceQuantityRequestDto incomingResourceInProduct, Product product) {
    return resourceInProductRepository
        .findByResourceIdAndProductId(incomingResourceInProduct.getId(), product.getId())
        .orElse(null);
  }

  private ResourceInProduct createResourceInProduct(
      ResourceQuantityRequestDto incomingResourceInProduct, Resource resource, Product product) {
    ResourceInProduct resourceInProduct = new ResourceInProduct();
    resourceInProduct.setResource(resource);
    resourceInProduct.setQuantity(incomingResourceInProduct.getQuantity());
    resourceInProduct.setProduct(product);
    return resourceInProduct;
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
