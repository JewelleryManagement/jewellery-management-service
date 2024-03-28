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
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductsInOrganizationResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.organization.OrganizationNotOwnerException;
import jewellery.inventory.mapper.ProductInOrganizationMapper;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.repository.ResourceInProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ProductInOrganizationService implements EntityFetcher {
  private final OrganizationService organizationService;
  private final ProductService productService;
  private final ProductInOrganizationMapper mapper;
  private final ResourceInOrganizationService resourceInOrganizationService;
  private final ResourceInProductRepository resourceInProductRepository;
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  @Transactional
  @LogUpdateEvent(eventType = EventType.ORGANIZATION_PRODUCT_UPDATE)
  public ProductsInOrganizationResponseDto updateProduct(
      UUID productId, ProductRequestDto productRequestDto) {

    Organization organization = organizationService.getOrganization(productRequestDto.getOwnerId());
    organizationService.validateUserInOrganization(organization);

    organizationService.validateCurrentUserPermission(
        organization, OrganizationPermission.EDIT_PRODUCT);

    Product product = productService.getProduct(productId);
    throwExceptionIfOrganizationNotOwner(organization.getId(),product);
    productService.throwExceptionIfProductIsSold(product);
    moveQuantityFromResourcesInProductToResourcesInOrganization(product);
    productService.disassembleProductContent(product);

    setProductFields(productRequestDto, organization, product);
    productRepository.save(product);

    addProductsContentToProduct(productRequestDto, product);
    addResourcesToProduct(productRequestDto, organization, product);

    return mapper.mapToProductsInOrganizationResponseDto(
        organization, List.of(productMapper.mapToProductResponseDto(product)));
  }

  public ProductsInOrganizationResponseDto getProductsInOrganization(UUID organizationId) {
    Organization organization = organizationService.getOrganization(organizationId);
    organizationService.validateUserInOrganization(organization);

    return mapper.mapToProductsInOrganizationResponseDto(
        organization, productService.getProductsResponse(organization.getProductsOwned()));
  }

  @Transactional
  @LogCreateEvent(eventType = EventType.ORGANIZATION_PRODUCT_CREATE)
  public ProductsInOrganizationResponseDto createProductInOrganization(
      ProductRequestDto productRequestDto) {
    Organization organization = organizationService.getOrganization(productRequestDto.getOwnerId());

    organizationService.validateCurrentUserPermission(
        organization, OrganizationPermission.CREATE_PRODUCT);

    Product product = persistProductWithoutResourcesAndProducts(productRequestDto, organization);
    addProductsContentToProduct(productRequestDto, product);
    addResourcesToProduct(productRequestDto, organization, product);

    return mapper.mapToProductsInOrganizationResponseDto(
        organization, productService.getProductsResponse(List.of(product)));
  }

  @Transactional
  @LogDeleteEvent(eventType = EventType.ORGANIZATION_PRODUCT_DISASSEMBLY)
  public void deleteProductInOrganization(UUID productId,UUID organizationId) {
    organizationService.validateCurrentUserPermission(
        organizationService.getOrganization(organizationId),
        OrganizationPermission.DISASSEMBLE_PRODUCT);

    Product forDeleteProduct = productService.getProduct(productId);

    throwExceptionIfOrganizationNotOwner(organizationId, forDeleteProduct);
    productService.throwExceptionIfProductIsSold(forDeleteProduct);
    productService.throwExceptionIfProductIsPartOfAnotherProduct(productId, forDeleteProduct);

    List<Product> subProducts =
        getProductsInProduct(
            forDeleteProduct.getProductsContent().stream().map(Product::getId).toList(),
            forDeleteProduct);

    removeProductsContentFromProduct(subProducts);
    moveQuantityFromResourcesInProductToResourcesInOrganization(forDeleteProduct);
    productService.deleteProductById(productId);
  }

  private Product persistProductWithoutResourcesAndProducts(
      ProductRequestDto productRequestDto, Organization organization) {
    Product product = getProductWithoutResourcesAndProduct(productRequestDto, organization);
    productService.saveProduct(product);
    return product;
  }

  private Product getProductWithoutResourcesAndProduct(
      ProductRequestDto productRequestDto, Organization organization) {
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
  }

  private void addProductsContentToProduct(ProductRequestDto productRequestDto, Product product) {
    if (productRequestDto.getProductsContent() != null) {
      product.setProductsContent(
          getProductsInProduct(productRequestDto.getProductsContent(), product));
      productService.saveProduct(product);
    }
  }

  private void removeProductsContentFromProduct(List<Product> subProducts) {
    if (!subProducts.isEmpty()) {
      for (Product subProduct : subProducts) {
        subProduct.setContentOf(null);
        productService.saveProduct(subProduct);
      }
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
            } else {
              throw new OrganizationNotOwnerException(
                  parentProduct.getOrganization().getId(), product.getId());
            }
          });
    }
    return products;
  }

  private void addResourcesToProduct(
      ProductRequestDto productRequestDto, Organization organization, Product product) {

    List<ResourceInProduct> resourcesInProducts =
        transferResourcesQuantitiesFromOrganizationToProduct(
            organization, productRequestDto.getResourcesContent(), product);
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
    return resourcesForAdd;
  }

  private ResourceInProduct transferSingleResourceQuantityFromOrganizationToProduct(
      Organization organization,
      ResourceQuantityRequestDto incomingResourceInProduct,
      Product product) {

    ResourceInOrganization resourceInOrganization =
        resourceInOrganizationService.findResourceInOrganizationOrThrow(
            organization, incomingResourceInProduct.getResourceId());

    checkResourceAvailability(resourceInOrganization, incomingResourceInProduct);

    resourceInOrganizationService.removeQuantityFromResource(
        organization.getId(),
        incomingResourceInProduct.getResourceId(),
        incomingResourceInProduct.getQuantity());

    return createResourceInProduct(
        incomingResourceInProduct.getQuantity(), resourceInOrganization.getResource(), product);
  }

  private ResourceInProduct createResourceInProduct(
      BigDecimal quantity, Resource resource, Product product) {
    ResourceInProduct resourceInProduct = new ResourceInProduct();
    resourceInProduct.setResource(resource);
    resourceInProduct.setQuantity(quantity);
    resourceInProduct.setProduct(product);
    return resourceInProduct;
  }

  private void checkResourceAvailability(
      ResourceInOrganization resourceInOrganization,
      ResourceQuantityRequestDto incomingResourceInProduct) {
    if (resourceInOrganization.getQuantity().compareTo(incomingResourceInProduct.getQuantity())
        < 0) {
      throw new InsufficientResourceQuantityException(
          incomingResourceInProduct.getQuantity(), resourceInOrganization.getQuantity());
    }
  }

  private void throwExceptionIfOrganizationNotOwner(UUID organizationId, Product product) {
    if (!organizationId.equals(product.getOrganization().getId())) {
      throw new OrganizationNotOwnerException(organizationId, product.getId());
    }
  }

  private ResourceInOrganizationRequestDto getResourceInOrganizationRequest(
      Organization owner, ResourceInProduct resourceInProduct) {

    return ResourceInOrganizationRequestDto.builder()
        .organizationId(owner.getId())
        .resourceId(resourceInProduct.getResource().getId())
        .quantity(resourceInProduct.getQuantity())
        .build();
  }

  private void moveQuantityFromResourcesInProductToResourcesInOrganization(Product product) {
    List<ResourceInProduct> resourcesInProduct = product.getResourcesContent();
    resourcesInProduct.forEach(
        resourceInProduct -> {
          resourceInOrganizationService.addResourceToOrganization(
              getResourceInOrganizationRequest(product.getOrganization(), resourceInProduct));
          resourceInProductRepository.delete(resourceInProduct);
        });
    product.setResourcesContent(null);
  }

  @Override
  public Object fetchEntity(Object... ids) {
    Product product = productRepository.findById((UUID) ids[0]).orElse(null);
    if (product == null) {
      return null;
    }
    return mapper.mapToProductsInOrganizationResponseDto(
        product.getOrganization(), List.of(productMapper.mapToProductResponseDto(product)));
  }
}
