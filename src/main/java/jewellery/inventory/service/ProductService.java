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
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService implements EntityFetcher {
  private static final Logger logger = Logger.getLogger(ProductService.class);
  private static final String NEW_OWNER_ID = "}. New owner with ID: {";
  private static final String PRODUCT_ID = "}, Product ID: {";

  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final ResourceInUserRepository resourceInUserRepository;
  private final ImageService imageService;
  private final ResourceInUserService resourceInUserService;
  private final ProductMapper productMapper;

  @Transactional
  @LogCreateEvent(eventType = EventType.PRODUCT_CREATE)
  public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
    User owner = getUser(productRequestDto.getOwnerId());
    Product product = persistProductWithoutResourcesAndProducts(productRequestDto, owner);
    addProductsContentToProduct(productRequestDto, product);
    addResourcesToProduct(productRequestDto, owner, product);
    logger.info("Product created with ID: {" + product.getId() + "}");
    return productMapper.mapToProductResponseDto(product);
  }

  public ProductReturnResponseDto getProductReturnResponseDto(
      SaleResponseDto sale, Product product) {
    return productMapper.mapToProductReturnResponseDto(sale, product);
  }

  public List<ProductResponseDto> getAllProducts() {
    List<Product> products = productRepository.findAll();
    logger.info("Get all products");
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  public List<ProductResponseDto> getByOwner(UUID ownerId) {
    List<Product> products = productRepository.findAllByOwnerId(ownerId);
    logger.info("Get product by owner with ID: {" + ownerId + "}");
    return products.stream().map(productMapper::mapToProductResponseDto).toList();
  }

  public Product getProduct(UUID id) {
    logger.info("Get product by ID: {" + id + "}");
    return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
  }

  public ProductResponseDto getProductResponse(UUID id) {
    logger.debug("Get productResponse by ID: {" + id + "}");
    return productMapper.mapToProductResponseDto(getProduct(id));
  }

  public void updateProductOwnerAndSale(Product product, User newOwner, Sale sale) {
    updateProductOwnerRecursively(product, newOwner);
    if (sale != null) {
      product.setPartOfSale(sale);
      logger.debug(
          "Updated product owner and sale for product with ID: {"
              + product.getId()
              + NEW_OWNER_ID
              + newOwner.getId()
              + "}, Sale with ID: {"
              + sale.getId()
              + "}");
    } else {
      product.setPartOfSale(null);
      logger.debug(
          "Updated product owner without sale for product with ID: {"
              + product.getId()
              + NEW_OWNER_ID
              + newOwner.getId()
              + "}, Sale set to null");
    }

    productRepository.save(product);
  }

  private void updateProductOwnerRecursively(Product product, User newOwner) {
    product.setOwner(newOwner);
    logger.debug(
        "Updated owner for product with ID: {"
            + product.getId()
            + NEW_OWNER_ID
            + newOwner.getId()
            + "}");
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

    throwExceptionIfProductIsSold(id, product);
    throwExceptionIfProductIsPartOfAnotherProduct(id, product);

    moveResourceInProductToResourceInUser(product);
    disassembleProductContent(product);
    deleteImageWhenAttached(id, product);

    logger.info("Delete product by ID: {" + id + "}");
    productRepository.deleteById(id);
  }

  @LogUpdateEvent(eventType = EventType.PRODUCT_TRANSFER)
  public ProductResponseDto transferProduct(UUID productId, UUID recipientId) {
    Product productForChangeOwner = getProductForTransfer(recipientId, productId);
    updateProductOwnerRecursively(productForChangeOwner, getUser(recipientId));
    logger.info(
        "Transferred product with ID {"
            + productId
            + "} to new owner with ID {"
            + recipientId
            + "}");
    productRepository.save(productForChangeOwner);
    return productMapper.mapToProductResponseDto(productForChangeOwner);
  }

  private void deleteImageWhenAttached(UUID id, Product product) throws IOException {
    if (product.getImage() != null) {
      logger.debug("Deleted image for product with ID: {" + product.getId() + "}");
      imageService.deleteImage(id);
    }
  }

  private void throwExceptionIfProductIsPartOfAnotherProduct(UUID id, Product product) {
    if (product.getContentOf() != null) {
      logger.error(
          "Product with ID {" + id + "} is part of another product and cannot be deleted.");
      throw new ProductIsContentException(id);
    }
  }

  private void throwExceptionIfProductOwnerEqualsRecipient(Product product, UUID recipientId) {
    if (product.getOwner().getId().equals(recipientId)) {
      logger.error(
          "Product owner is the same as the recipient. Product ID: {"
              + product.getId()
              + "}, Owner ID: {"
              + product.getOwner().getId()
              + "}, Recipient ID: {"
              + recipientId
              + "}");
      throw new ProductOwnerEqualsRecipientException(recipientId);
    }
  }

  private void throwExceptionIfProductIsSold(UUID id, Product product) {
    if (product.getPartOfSale() != null) {
      logger.error("Product with ID {" + id + "} is part of a sale");
      throw new ProductIsSoldException(id);
    }
  }

  private void disassembleProductContent(Product product) {
    if (product.getProductsContent() != null) {
      logger.debug("Disassembling product content for product with ID: {" + product.getId() + "}");

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

    logger.debug(
        "Moving resources from product with ID {"
            + product.getId()
            + "} to owner with ID {"
            + owner.getId()
            + "}");

    resourcesInProduct.forEach(
        resourceInProduct ->
            resourceInUserService.addResourceToUserNoLog(
                getResourceInUserRequest(owner, resourceInProduct)));
  }

  private ResourceInUserRequestDto getResourceInUserRequest(
      User owner, ResourceInProduct resourceInProduct) {
    logger.debug("Getting resourceInUserRequest for user with Id: {" + owner.getId() + "}");
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
    logger.debug(
        "Getting product for transfer with ID: {"
            + productId
            + "}, recipient ID: {"
            + recipientId
            + "}");

    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    validateProductForChangeOwner(recipientId, product);
    logger.debug("Product for transfer retrieved successfully.");
    return product;
  }

  private void validateProductForChangeOwner(UUID recipientId, Product productForChangeOwner) {
    logger.debug(
        "Validating product for change owner. Product ID: {"
            + productForChangeOwner.getId()
            + "}, Recipient ID: {"
            + recipientId
            + "}");
    throwExceptionIfProductIsPartOfAnotherProduct(
        productForChangeOwner.getId(), productForChangeOwner);
    throwExceptionIfProductIsSold(productForChangeOwner.getId(), productForChangeOwner);
    throwExceptionIfProductOwnerEqualsRecipient(productForChangeOwner, recipientId);
    logger.debug("Product validation for change owner successful.");
  }

  private List<Product> getProductsInProduct(
      List<UUID> productsIdInRequest, Product parentProduct) {
    logger.debug("Getting products in product. Parent Product ID: {" + parentProduct.getId() + "}");

    List<Product> products = new ArrayList<>();
    if (productsIdInRequest != null) {
      productsIdInRequest.forEach(
          productId -> {
            logger.debug("Processing product with ID: {" + productId + "}");
            Product product =
                productRepository
                    .findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            if (product.getOwner().getId().equals(parentProduct.getOwner().getId())) {
              product.setContentOf(parentProduct);
              products.add(product);
              logger.debug("Added product with ID {" + productId + "} to the list.");
            } else {
              logger.error(
                  "User with ID {"
                      + parentProduct.getOwner().getId()
                      + "} is not the owner of the product with ID {"
                      + productId
                      + "}.");
              throw new UserNotOwnerException(parentProduct.getOwner().getId(), product.getId());
            }
          });
    }
    return products;
  }

  private User getUser(UUID userId) {
    logger.debug("Getting user with ID: {" + userId + "}");
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
  }

  private Product persistProductWithoutResourcesAndProducts(
      ProductRequestDto productRequestDto, User user) {
    logger.debug(
        "Persisting product without resources and products for user with ID: {"
            + user.getId()
            + "}");
    Product product = getProductWithoutResourcesAndProduct(productRequestDto, user);
    productRepository.save(product);
    logger.debug("Product persisted successfully. Product ID: {" + product.getId() + "}");

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
    logger.debug("Getting authors for product.");
    List<UUID> authorsIds = productRequestDto.getAuthors();
    List<User> authors = new ArrayList<>();
    authorsIds.forEach(
        id -> {
          logger.debug("Processing author with ID: {" + id + "}");
          User author =
              userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
          authors.add(author);
          logger.debug("Author with ID {" + id + "} added to the list.");
        });
    return authors;
  }

  private void addProductsContentToProduct(ProductRequestDto productRequestDto, Product product) {
    logger.debug("Adding products content to product. Product ID: {" + product.getId() + "}");

    if (productRequestDto.getProductsContent() != null) {
      product.setProductsContent(
          getProductsInProduct(productRequestDto.getProductsContent(), product));
      productRepository.save(product);
      logger.debug(
          "Products content added successfully to product. Count: {"
              + productRequestDto.getProductsContent().size()
              + "}");
    }
  }

  private void addResourcesToProduct(
      ProductRequestDto productRequestDto, User user, Product product) {
    logger.debug("Adding resources to product. Product ID: {" + product.getId() + "}");

    List<ResourceInProduct> resourcesInProducts =
        transferResourcesQuantitiesFromUserToProduct(
            user, productRequestDto.getResourcesContent(), product);
    product.setResourcesContent(resourcesInProducts);
    logger.debug(
        "Resources added successfully to product. Count: {" + resourcesInProducts.size() + "}");
  }

  private List<ResourceInProduct> transferResourcesQuantitiesFromUserToProduct(
      User owner, List<ResourceQuantityRequestDto> incomingResourceInProductList, Product product) {
    logger.debug(
        "Transferring resources quantities from user to product. User ID: {"
            + owner.getId()
            + PRODUCT_ID
            + product.getId()
            + "}");

    return incomingResourceInProductList.stream()
        .map(
            incomingResourceInProduct ->
                transferSingleResourceQuantityFromUserToProduct(
                    owner, incomingResourceInProduct, product))
        .toList();
  }

  private ResourceInProduct transferSingleResourceQuantityFromUserToProduct(
      User owner, ResourceQuantityRequestDto incomingResourceInProduct, Product product) {
    logger.debug(
        "Transferring single resource quantity from user to product. User ID: {"
            + owner.getId()
            + PRODUCT_ID
            + product.getId()
            + "}");

    ResourceInUser resourceInUser = getResourceInUser(owner, incomingResourceInProduct.getId());
    resourceInUserService.removeQuantityFromResourceNoLog(
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
    logger.debug(
        "Resource in product created successfully. Resource ID: {"
            + resourceInProduct.getResource().getId()
            + PRODUCT_ID
            + resourceInProduct.getProduct().getId()
            + "}");

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
