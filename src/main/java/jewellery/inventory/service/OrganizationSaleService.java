package jewellery.inventory.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.dto.request.ProductDiscountRequestDto;
import jewellery.inventory.dto.request.PurchasedResourceQuantityRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.OrganizationSaleResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.ResourceReturnResponseDto;
import jewellery.inventory.exception.not_found.SaleNotFoundException;
import jewellery.inventory.exception.organization.OrganizationNotOwnerException;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.ProductNotSoldException;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationSaleService {

  private static final Logger logger = LogManager.getLogger(OrganizationSaleService.class);

  private final SaleService saleService;
  private final SaleMapper saleMapper;
  private final OrganizationService organizationService;
  private final UserService userService;
  private final ResourceInOrganizationService resourceInOrganizationService;
  private final SaleRepository saleRepository;
  private final ProductService productService;

  @LogCreateEvent(eventType = EventType.ORGANIZATION_CREATE_SALE)
  @Transactional
  public OrganizationSaleResponseDto createSale(SaleRequestDto saleRequestDto) {

    saleService.throwExceptionIfNoResourcesAndProductsInRequest(saleRequestDto);

    Sale sale =
        saleMapper.mapSaleFromOrganization(
            saleRequestDto,
            organizationService.getOrganization(saleRequestDto.getSellerId()),
            userService.getUser(saleRequestDto.getBuyerId()),
            saleService.getProductsFromSaleRequestDto(saleRequestDto),
            saleService.getResourcesFromSaleRequestDto(saleRequestDto));

    organizationService.validateUserInOrganization(
        organizationService.getOrganization(saleRequestDto.getSellerId()));
    organizationService.validateCurrentUserPermission(
        organizationService.getOrganization(saleRequestDto.getSellerId()),
        OrganizationPermission.CREATE_SALE);

    throwExceptionIfProductIsSold(saleRequestDto);
    throwExceptionIfProductIsPartOfAnotherProduct(saleRequestDto);
    throwExceptionIfSellerNotProductOwner(saleRequestDto);
    throwExceptionIfResourceIsNotOwned(saleRequestDto);

    Sale createdSale = saleRepository.save(sale);
    saleService.setProductPriceDiscountSalePriceAndSale(sale);
    saleService.updateProductOwnersAndSale(
        sale.getProducts(), saleRequestDto.getBuyerId(), createdSale);
    removeQuantityFromResourcesInOrganization(sale);
    saleService.setFieldsOfResourcesAfterSale(sale);
    logger.info("Sale created successfully. Sale ID: {}", createdSale.getId());
    return saleMapper.mapToOrganizationSaleResponseDto(createdSale);
  }

  @LogCreateEvent(eventType = EventType.ORGANIZATION_SALE_RETURN_PRODUCT)
  public ProductReturnResponseDto returnProduct(UUID productId) {
    Product productToReturn = productService.getProduct(productId);

    throwExceptionIfProductIsPartOfAnotherProduct(productToReturn);
    throwExceptionIfProductNotSold(productToReturn);

    Sale sale = getSaleById(productToReturn.getPartOfSale().getSale().getId());

    organizationService.validateUserInOrganization(
        organizationService.getOrganization(sale.getOrganizationSeller().getId()));
    organizationService.validateCurrentUserPermission(
        organizationService.getOrganization(sale.getOrganizationSeller().getId()),
        OrganizationPermission.RETURN_PRODUCT);

    removeProductFromSale(productId, sale);
    updateProductsOrganizationOwner(productToReturn, sale.getOrganizationSeller(), sale);
    deleteSaleIfProductsAndResourcesAreEmpty(sale);
    logger.info("Product returned successfully. Product ID: {}", productId);
    return saleService.validateSaleAfterReturnProduct(sale, productToReturn);
  }

  @LogCreateEvent(eventType = EventType.ORGANIZATION_SALE_RETURN_RESOURCE)
  @Transactional
  public ResourceReturnResponseDto returnResource(UUID saleId, UUID resourceId) {
    Sale sale = getSaleById(saleId);
    PurchasedResourceInUser resourceToReturn = saleService.getPurchasedResource(resourceId, saleId);

    organizationService.validateUserInOrganization(
        organizationService.getOrganization(sale.getOrganizationSeller().getId()));
    organizationService.validateCurrentUserPermission(
        organizationService.getOrganization(sale.getOrganizationSeller().getId()),
        OrganizationPermission.RETURN_RESOURCE);

    returnResourceFromSaleToOrganization(sale, resourceToReturn);
    removeResourceFromSale(sale, resourceToReturn);

    deleteSaleIfProductsAndResourcesAreEmpty(sale);
    logger.info("Resource returned successfully. Resource ID: {}", resourceId);
    return saleService.validateSaleAfterReturnResource(sale, resourceToReturn);
  }

  public List<OrganizationSaleResponseDto> getAllSales() {
    logger.debug("Fetching all Sales from organization");
    return saleRepository.findAll().stream()
        .map(saleMapper::mapToOrganizationSaleResponseDto)
        .toList();
  }

  public OrganizationSaleResponseDto getSale(UUID saleId) {
    logger.debug("Fetching a Sale from organization");
    return saleMapper.mapToOrganizationSaleResponseDto(getSaleById(saleId));
  }

  public Sale getSaleById(UUID saleId) {
    return saleRepository.findById(saleId).orElseThrow(() -> new SaleNotFoundException(saleId));
  }

  private void returnResourceFromSaleToOrganization(
      Sale sale, PurchasedResourceInUser resourceToReturn) {
    ResourceInOrganization resourceInOrganization =
        resourceInOrganizationService.getResourceInOrganization(
            sale.getOrganizationSeller(), resourceToReturn.getResource());
    resourceInOrganization.setQuantity(
        resourceInOrganization.getQuantity().add(resourceToReturn.getQuantity()));
  }

  private void updateProductsOrganizationOwner(
      Product product, Organization organization, Sale sale) {
    updateProductsOrganizationOwnerRecursively(product, organization);

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
        "Updated products organization owner and sale for product with ID: {}. New organizations owner with ID: {}, Sale with ID: {}",
        product.getId(),
        product.getOrganization().getId(),
        product.getPartOfSale() != null ? product.getPartOfSale().getId() : null);
    productService.saveProduct(product);
  }

  private void updateProductsOrganizationOwnerRecursively(
      Product product, Organization organization) {
    product.setOrganization(organization);
    logger.debug(
        "Updated organization owner for product with ID: {}. New organization owner with ID: {}",
        product.getId(),
        organization.getId());
    if (product.getProductsContent() != null) {
      List<Product> subProducts = product.getProductsContent();
      for (Product subProduct : subProducts) {
        updateProductsOrganizationOwnerRecursively(subProduct, organization);
      }
    }
  }

  private void removeQuantityFromResourcesInOrganization(Sale sale) {
    if (sale.getResources() != null) {
      for (PurchasedResourceInUser resource : sale.getResources()) {
        ResourceInOrganization resourceInOrganization =
            resourceInOrganizationService.getResourceInOrganization(
                sale.getOrganizationSeller(), resource.getResource());
        resourceInOrganizationService.removeQuantityFromResource(
            sale.getOrganizationSeller().getId(),
            resourceInOrganization.getResource().getId(),
            resource.getQuantity());
      }
    }
  }

  private void throwExceptionIfSellerNotProductOwner(SaleRequestDto saleRequestDto) {
    if (saleRequestDto.getProducts() != null) {
      for (ProductDiscountRequestDto productDiscountRequestDto : saleRequestDto.getProducts()) {
        Product product = productService.getProduct(productDiscountRequestDto.getProductId());
        if (!product.getOrganization().getId().equals(saleRequestDto.getSellerId())) {
          throw new OrganizationNotOwnerException(
              product.getOwner().getId(), saleRequestDto.getSellerId());
        }
      }
    }
  }

  private void throwExceptionIfResourceIsNotOwned(SaleRequestDto saleRequestDto) {
    if (saleRequestDto.getResources() != null) {
      for (PurchasedResourceQuantityRequestDto resource : saleRequestDto.getResources()) {
        resourceInOrganizationService.findResourceInOrganizationOrThrow(
            organizationService.getOrganization(saleRequestDto.getSellerId()),
            resource.getResourceAndQuantity().getResourceId());
      }
    }
  }

  private void throwExceptionIfProductIsSold(SaleRequestDto saleRequestDto) {
    if (saleRequestDto.getProducts() != null) {
      for (ProductDiscountRequestDto productDiscountRequestDto : saleRequestDto.getProducts()) {
        Product product = productService.getProduct(productDiscountRequestDto.getProductId());
        if (product.getPartOfSale() != null) {
          throw new ProductIsSoldException(product.getId());
        }
      }
    }
  }

  private void throwExceptionIfProductIsPartOfAnotherProduct(SaleRequestDto saleRequestDto) {
    if (saleRequestDto.getProducts() != null) {
      for (ProductDiscountRequestDto productDiscountRequestDto : saleRequestDto.getProducts()) {
        Product product = productService.getProduct(productDiscountRequestDto.getProductId());
        if (product.getContentOf() != null) {
          throw new ProductIsContentException(product.getId());
        }
      }
    }
  }

  private void throwExceptionIfProductIsPartOfAnotherProduct(Product product) {
    if (product.getContentOf() != null) {
      throw new ProductIsContentException(product.getId());
    }
  }

  private void throwExceptionIfProductNotSold(Product product) {
    if (product.getPartOfSale() == null) {
      throw new ProductNotSoldException(product.getId());
    }
  }

  private void deleteSaleIfProductsAndResourcesAreEmpty(Sale sale) {
    if (sale.getProducts().isEmpty() && sale.getResources().isEmpty()) {
      logger.info(
          "Deleting sale with ID: {} since the both products list and resources list are empty.",
          sale.getId());
      saleRepository.deleteById(sale.getId());
    } else {
      logger.info(
          "Saving sale with ID: {} since the products list or the resources list are not empty.",
          sale.getId());
      saleRepository.save(sale);
    }
  }

  private void removeProductFromSale(UUID productId, Sale sale) {
    sale.getProducts()
        .removeIf(
            productPriceDiscount -> productPriceDiscount.getProduct().getId().equals(productId));
  }

  public void removeResourceFromSale(Sale sale, PurchasedResourceInUser resourceToReturn) {
    sale.getResources()
        .removeIf(
            purchasedResource ->
                purchasedResource
                    .getResource()
                    .getId()
                    .equals(resourceToReturn.getResource().getId()));
  }
}
