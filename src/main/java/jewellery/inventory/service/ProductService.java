package jewellery.inventory.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.*;
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

  public List<ProductResponseDto> getAllProducts() {
    List<Product> products = productRepository.findAll();
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  public List<ProductResponseDto> getByOwner(UUID ownerId) {
    List<Product> products = productRepository.findAllByOwnerId(ownerId);
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  public ProductResponseDto getProduct(UUID id) {
    Product product =
        productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

    return productMapper.mapToProductResponseDto(product);
  }

  @Transactional
  public void deleteProduct(UUID id) {

    Product product =
        productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

    throwExceptionIfProductIsSold(id, product);
    throwExceptionIfProductIsPartOfAnotherProduct(id, product);

    moveResourceInProductToResourceInUser(product);
    disassembleProductContent(product);

    productRepository.deleteById(id);
  }

  public ProductResponseDto transferProduct(UUID recipientId, UUID productId) {
    Product productForChangeOwner = getProductForTransfer(recipientId, productId);
    productForChangeOwner.setOwner(getUser(recipientId));
    productRepository.save(productForChangeOwner);
    return productMapper.mapToProductResponseDto(productForChangeOwner);
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

  private void throwExceptionIfProductIsSold(UUID id, Product product) {
    if (product.getPartOfSale() != null) {
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
    throwExceptionIfProductIsSold(productForChangeOwner.getId(), productForChangeOwner);
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
              throw new ProductOwnerNotSeller(parentProduct.getOwner().getId(), product.getId());
            }
          });
    }

    return products;
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

  private Product getProduct(ProductRequestDto productRequestDto, User user) {
    Product product = new Product();
    product.setOwner(user);
    product.setAuthors(getAuthors(productRequestDto));
    product.setPartOfSale(null);
    product.setDescription(productRequestDto.getDescription());
    product.setSalePrice(productRequestDto.getSalePrice());
    product.setProductionNumber(productRequestDto.getProductionNumber());
    product.setCatalogNumber(productRequestDto.getCatalogNumber());
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
    resourceInUserService.removeQuantityFromResource(
        owner.getId(),
        resourceInUser.getResource().getId(),
        incomingResourceInProduct.getQuantity());
    return createResourceInProduct(
        incomingResourceInProduct, resourceInUser.getResource(), product);
  }

  private ResourceInProduct createResourceInProduct(
      ResourceQuantityRequestDto incomingResourceInProduct, Resource resource, Product product) {
    ResourceInProduct resourceInProduct = new ResourceInProduct();
    resourceInProduct.setResource(resource);
    resourceInProduct.setQuantity(incomingResourceInProduct.getQuantity());
    resourceInProduct.setProduct(product);
    return resourceInProduct;
  }
}
