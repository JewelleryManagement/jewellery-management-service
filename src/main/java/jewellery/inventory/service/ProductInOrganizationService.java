package jewellery.inventory.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductsInOrganizationResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.organization.OrganizationNotOwnerException;
import jewellery.inventory.exception.organization.ProductIsNotPartOfOrganizationException;
import jewellery.inventory.mapper.ProductInOrganizationMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.repository.ResourceInProductRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ProductInOrganizationService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(ProductInOrganizationService.class);
  private final OrganizationService organizationService;
  private final ProductService productService;
  private final ProductInOrganizationMapper mapper;
  private final ResourceInOrganizationService resourceInOrganizationService;
  private final ResourceInProductRepository resourceInProductRepository;
  private final ProductRepository productRepository;

  public ProductsInOrganizationResponseDto getProductsInOrganization(UUID organizationId) {
    Organization organization = organizationService.getOrganization(organizationId);
    organizationService.validateUserInOrganization(organization);

    return mapper.mapToProductsInOrganizationResponseDto(
        organization, productService.getProductsResponse(organization.getProductsOwned()));
  }

  @LogUpdateEvent(eventType = EventType.ORGANIZATION_PRODUCT_TRANSFER)
  public ProductsInOrganizationResponseDto transferProduct(UUID productId, UUID recipientId) {
    Product product = productService.getProduct(productId);
    productService.throwExceptionIfProductIsSold(product);
    productService.throwExceptionIfProductIsPartOfAnotherProduct(productId, product);
    Organization recipient = organizationService.getOrganization(recipientId);

    organizationService.validateCurrentUserPermission(
        product.getOrganization(), OrganizationPermission.TRANSFER_PRODUCT);

    updateProductOrganizationRecursively(product, recipient);
    logger.info(
        "Transferred product with ID {} to new organization with ID {}", productId, recipientId);
    productRepository.save(product);

    return mapper.mapToProductsInOrganizationResponseDto(
        recipient, productService.getProductsResponse(List.of(product)));
  }

  @Transactional
  @LogUpdateEvent(eventType = EventType.ORGANIZATION_PRODUCT_UPDATE)
  public ProductsInOrganizationResponseDto updateProduct(
      UUID productId, ProductRequestDto productRequestDto) {

    Organization organization = organizationService.getOrganization(productRequestDto.getOwnerId());

    organizationService.validateCurrentUserPermission(
        organization, OrganizationPermission.EDIT_PRODUCT);

    Product product = productService.getProduct(productId);
    throwExceptionIfOrganizationNotOwner(organization.getId(), product);
    productService.throwExceptionIfProductIsSold(product);
    moveQuantityFromResourcesInProductToResourcesInOrganization(product);
    productService.disassembleProductContent(product);

    setProductFields(productRequestDto, organization, product);
    productRepository.save(product);

    return addProductContents(organization, productRequestDto, product);
  }

  @Transactional
  @LogCreateEvent(eventType = EventType.ORGANIZATION_PRODUCT_CREATE)
  public ProductsInOrganizationResponseDto createProductInOrganization(
      ProductRequestDto productRequestDto) {
    Organization organization = organizationService.getOrganization(productRequestDto.getOwnerId());

    organizationService.validateCurrentUserPermission(
        organization, OrganizationPermission.CREATE_PRODUCT);

    Product product = persistProductWithoutResourcesAndProducts(productRequestDto, organization);

    return addProductContents(organization, productRequestDto, product);
  }

  @Transactional
  @LogDeleteEvent(eventType = EventType.ORGANIZATION_PRODUCT_DISASSEMBLY)
  public void deleteProductInOrganization(UUID productId) {
    Product product = productService.getProduct(productId);
    validateProductIsOwnedOrganization(product);
    Organization organization = product.getOrganization();

    organizationService.validateCurrentUserPermission(
        organization, OrganizationPermission.DISASSEMBLE_PRODUCT);

    productService.throwExceptionIfProductIsSold(product);
    productService.throwExceptionIfProductIsPartOfAnotherProduct(productId, product);

    moveQuantityFromResourcesInProductToResourcesInOrganization(product);

    productService.disassembleProductContent(product);
    productService.deleteProductById(productId);
  }

  private void validateProductIsOwnedOrganization(Product product) {
    if (product.getOrganization() == null) {
      throw new ProductIsNotPartOfOrganizationException(product.getId());
    }
  }

  private ProductsInOrganizationResponseDto addProductContents(
      Organization organization, ProductRequestDto productRequestDto, Product product) {
    addProductsContentToProduct(productRequestDto, product);
    addResourcesToProduct(productRequestDto, organization, product);

    return mapper.mapToProductsInOrganizationResponseDto(
        organization, productService.getProductsResponse(List.of(product)));
  }

  private Product persistProductWithoutResourcesAndProducts(
      ProductRequestDto productRequestDto, Organization organization) {
    Product product = getProductWithoutResourcesAndProduct(productRequestDto, organization);
    productService.saveProduct(product);
    return product;
  }

  private Product getProductWithoutResourcesAndProduct(
      ProductRequestDto productRequestDto, Organization organization) {
    logger.debug("Creating product without resources and products");
    Product product = new Product();
    setProductFields(productRequestDto, organization, product);
    return product;
  }

  private void setProductFields(
      ProductRequestDto productRequestDto, Organization organization, Product product) {
    product.setOwner(null);
    product.setOrganization(organization);
    product.setAuthors(productService.getAuthors(productRequestDto));
    product.setPartOfSale(null);
    product.setDescription(productRequestDto.getDescription());
    product.setProductionNumber(productRequestDto.getProductionNumber());
    product.setCatalogNumber(productRequestDto.getCatalogNumber());
    product.setAdditionalPrice(productRequestDto.getAdditionalPrice());
    product.setProductsContent(new ArrayList<>());
    product.setResourcesContent(new ArrayList<>());
    logger.debug("Product fields have been set successfully for product: {}", product);
  }

  private void addProductsContentToProduct(ProductRequestDto productRequestDto, Product product) {
    if (productRequestDto.getProductsContent() != null) {
      product.setProductsContent(
          getProductsInProduct(productRequestDto.getProductsContent(), product));
      logger.debug("Products content added successfully to product: {}", product);
      productService.saveProduct(product);
    }
  }

  private List<Product> getProductsInProduct(
      List<UUID> productsIdInRequest, Product parentProduct) {
    List<Product> products = new ArrayList<>();
    if (productsIdInRequest != null) {
      productsIdInRequest.forEach(
          productId -> {
            Product product = productService.getProduct(productId);
            productService.throwExceptionIfProductIsPartOfItself(product, parentProduct.getId());
            productService.throwExceptionIfProductIsSold(product);
            if ((product.getOrganization() != null)
                && parentProduct
                    .getOrganization()
                    .getId()
                    .equals(product.getOrganization().getId())) {
              product.setContentOf(parentProduct);
              products.add(product);
              logger.debug("Added product '{}' to the list.", product);
            } else {
              logger.error(
                  "Organization with ID '{}' is not the owner of product with ID '{}'.",
                  parentProduct.getOrganization().getId(),
                  product.getId());
              throw new OrganizationNotOwnerException(
                  parentProduct.getOrganization().getId(), product.getId());
            }
          });
    }
    logger.debug("Products in product retrieved successfully.");
    return products;
  }

  private void addResourcesToProduct(
      ProductRequestDto productRequestDto, Organization organization, Product product) {

    List<ResourceInProduct> resourcesInProducts =
        transferResourcesQuantitiesFromOrganizationToProduct(
            organization, productRequestDto.getResourcesContent(), product);
    logger.debug("Resources added successfully to product: {}", product);

    product.setResourcesContent(resourcesInProducts);
  }

  private List<ResourceInProduct> transferResourcesQuantitiesFromOrganizationToProduct(
      Organization organization,
      List<ResourceQuantityRequestDto> incomingResourceInProductList,
      Product product) {
    List<ResourceInProduct> resourcesForAdd = new ArrayList<>();

    for (ResourceQuantityRequestDto resourceQuantity : incomingResourceInProductList) {
      resourcesForAdd.add(
          transferSingleResourceQuantityFromOrganizationToProduct(
              organization, resourceQuantity, product));
    }
    logger.debug("Resources quantities transferred successfully from organization to product.");
    return resourcesForAdd;
  }

  private ResourceInProduct transferSingleResourceQuantityFromOrganizationToProduct(
      Organization organization,
      ResourceQuantityRequestDto incomingResourceInProduct,
      Product product) {
    logger.debug(
        "Transferring single resource quantity from organization to product for product: {}",
        product);

    ResourceInOrganization resourceInOrganization =
        resourceInOrganizationService.findResourceInOrganizationOrThrow(
            organization, incomingResourceInProduct.getResourceId());

    checkResourceAvailability(resourceInOrganization, incomingResourceInProduct);

    resourceInOrganizationService.removeQuantityFromResource(
        organization.getId(),
        incomingResourceInProduct.getResourceId(),
        incomingResourceInProduct.getQuantity());

    logger.debug(
        "Quantity '{}' of resource '{}' removed from organization '{}'.",
        incomingResourceInProduct.getQuantity(),
        resourceInOrganization.getResource(),
        organization.getName());

    return createResourceInProduct(
        incomingResourceInProduct.getQuantity(), resourceInOrganization.getResource(), product);
  }

  private ResourceInProduct createResourceInProduct(
      BigDecimal quantity, Resource resource, Product product) {
    ResourceInProduct resourceInProduct = new ResourceInProduct();
    resourceInProduct.setResource(resource);
    resourceInProduct.setQuantity(quantity);
    resourceInProduct.setProduct(product);
    logger.debug("ResourceInProduct created successfully for product: {}", product);
    return resourceInProduct;
  }

  private void checkResourceAvailability(
      ResourceInOrganization resourceInOrganization,
      ResourceQuantityRequestDto incomingResourceInProduct) {
    if (resourceInOrganization.getQuantity().compareTo(incomingResourceInProduct.getQuantity())
        < 0) {
      logger.error(
          "Insufficient quantity of resource '{}' in organization. Requested: {}, Available: {}",
          resourceInOrganization.getResource(),
          incomingResourceInProduct.getQuantity(),
          resourceInOrganization.getQuantity());

      throw new InsufficientResourceQuantityException(
          incomingResourceInProduct.getQuantity(), resourceInOrganization.getQuantity());
    }
  }

  private void throwExceptionIfOrganizationNotOwner(UUID organizationId, Product product) {
    if (!organizationId.equals(product.getOrganization().getId())) {
      logger.error(
          "Organization with ID '{}' is not the owner of product with ID '{}'.",
          organizationId,
          product.getId());
      throw new OrganizationNotOwnerException(organizationId, product.getId());
    }
  }

  private void moveQuantityFromResourcesInProductToResourcesInOrganization(Product product) {
    List<ResourceInProduct> resourcesInProduct = product.getResourcesContent();
    resourcesInProduct.forEach(
        resourceInProduct -> {
          logger.info(
              "Adding resource: {} to organization: {} with quantity {}",
              resourceInProduct.getResource(),
              product.getOrganization(),
              resourceInProduct.getQuantity());
          resourceInOrganizationService.addResourceToOrganization(
              product.getOrganization(),
              resourceInProduct.getResource(),
              resourceInProduct.getQuantity(),
              BigDecimal.ZERO);

          resourceInProduct.setProduct(null);
          resourceInProductRepository.delete(resourceInProduct);
          logger.info("Resource deleted from product: {}", product);
        });
    product.setResourcesContent(null);
  }

  private void updateProductOrganizationRecursively(Product product, Organization newOrganization) {
    product.setOrganization(newOrganization);
    logger.debug(
        "Updated organization for product with ID: {}. New organization with ID: {}",
        product.getId(),
        newOrganization.getId());
    if (product.getProductsContent() != null) {
      List<Product> subProducts = product.getProductsContent();
      for (Product subProduct : subProducts) {
        updateProductOrganizationRecursively(subProduct, newOrganization);
      }
    }
  }

  @Override
  public Object fetchEntity(Object... ids) {
    Product product = productRepository.findById((UUID) ids[0]).orElse(null);
    if (product == null || product.getOrganization() == null) {
      return null;
    }
    return mapper.mapToProductsInOrganizationResponseDto(
        product.getOrganization(), productService.getProductsResponse(List.of(product)));
  }
}
