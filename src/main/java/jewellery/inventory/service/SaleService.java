package jewellery.inventory.service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.dto.request.PurchasedResourceQuantityRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.ResourceReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.not_found.ResourceNotFoundInSaleException;
import jewellery.inventory.exception.not_found.SaleNotFoundException;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.ProductNotSoldException;
import jewellery.inventory.exception.product.UserNotOwnerException;
import jewellery.inventory.exception.sale.EmptySaleException;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.PurchasedResourceInUserRepository;
import jewellery.inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleService {
  private static final Logger logger = LogManager.getLogger(SaleService.class);
  private final SaleRepository saleRepository;
  private final PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  private final SaleMapper saleMapper;
  private final ProductService productService;
  private final ResourceInUserService resourceInUserService;
  private final UserService userService;
  private final ResourceService resourceService;

  public List<SaleResponseDto> getAllSales() {
    logger.debug("Fetching all Sales");
    List<Sale> sales = saleRepository.findAll();
    return sales.stream().map(saleMapper::mapEntityToResponseDto).toList();
  }

  @LogCreateEvent(eventType = EventType.SALE_CREATE)
  @Transactional
  public SaleResponseDto createSale(SaleRequestDto saleRequestDto) {

    throwExceptionIfNoResourcesAndProductsInRequest(saleRequestDto);

    Sale sale =
        saleMapper.mapRequestToEntity(
            saleRequestDto,
            userService.getUser(saleRequestDto.getSellerId()),
            userService.getUser(saleRequestDto.getBuyerId()),
            getProductsFromSaleRequestDto(saleRequestDto),
            getResourcesFromSaleRequestDto(saleRequestDto));

    throwExceptionIfProductIsSold(sale.getProducts());
    throwExceptionIfSellerNotProductOwner(sale.getProducts(), saleRequestDto.getSellerId());
    throwExceptionIfProductIsPartOfAnotherProduct(sale.getProducts());
    throwExceptionIfResourceIsNotOwned(saleRequestDto);

    Sale createdSale = saleRepository.save(sale);
    setProductPriceDiscountSalePriceAndSale(createdSale);
    updateProductOwnersAndSale(sale.getProducts(), saleRequestDto.getBuyerId(), createdSale);
    removeQuantityFromResourcesInUser(sale);
    setFieldsOfResourcesAfterSale(sale);
    logger.info("Sale created successfully. Sale ID: {}", createdSale.getId());
    return saleMapper.mapEntityToResponseDto(createdSale);
  }

  @LogCreateEvent(eventType = EventType.SALE_RETURN_PRODUCT)
  public ProductReturnResponseDto returnProduct(UUID productId) {
    Product productToReturn = productService.getProduct(productId);

    throwExceptionIfProductIsPartOfAnotherProduct(productToReturn);
    throwExceptionIfProductNotSold(productToReturn);

    Sale sale = getSale(productToReturn.getPartOfSale().getSale().getId());

    sale.getProducts()
        .removeIf(
            productPriceDiscount -> productPriceDiscount.getProduct().getId().equals(productId));

    productService.updateProductOwnerAndSale(productToReturn, sale.getSeller(), null);
    deleteSaleIfProductsAndResourcesAreEmpty(sale);
    logger.info("Product returned successfully. Product ID: {}", productId);
    return validateSaleAfterReturnProduct(sale, productToReturn);
  }

  @LogCreateEvent(eventType = EventType.SALE_RETURN_RESOURCE)
  @Transactional
  public ResourceReturnResponseDto returnResource(UUID saleId, UUID resourceId) {
    Sale sale = getSale(saleId);
    PurchasedResourceInUser resourceToReturn = getPurchasedResource(resourceId, saleId);

    returnResourceFromSaleToUser(sale, resourceToReturn);
    removeResourceFromSale(sale, resourceToReturn);

    deleteSaleIfProductsAndResourcesAreEmpty(sale);
    logger.info("Resource returned successfully. Resource ID: {}", resourceId);

    return validateSaleAfterReturnResource(sale, resourceToReturn);
  }

  private static void removeResourceFromSale(Sale sale, PurchasedResourceInUser resourceToReturn) {
    sale.getResources()
        .removeIf(
            purchasedResource ->
                purchasedResource
                    .getResource()
                    .getId()
                    .equals(resourceToReturn.getResource().getId()));
  }

  private Sale getSale(UUID saleId) {
    return saleRepository.findById(saleId).orElseThrow(() -> new SaleNotFoundException(saleId));
  }

  private void throwExceptionIfProductIsPartOfAnotherProduct(List<ProductPriceDiscount> products) {
    for (ProductPriceDiscount productPriceDiscount : products) {
      Product product = productPriceDiscount.getProduct();
      if (product.getContentOf() != null) {
        throw new ProductIsContentException(product.getId());
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

  private void throwExceptionIfProductIsSold(List<ProductPriceDiscount> products) {
    for (ProductPriceDiscount productPriceDiscount : products) {
      Product product = productPriceDiscount.getProduct();
      if (product.getPartOfSale() != null) {
        throw new ProductIsSoldException(product.getId());
      }
    }
  }

  private void updateProductOwnersAndSale(
      List<ProductPriceDiscount> products, UUID buyerId, Sale sale) {
    User newOwner = userService.getUser(buyerId);

    for (ProductPriceDiscount productPriceDiscount : products) {
      Product product = productPriceDiscount.getProduct();
      productService.updateProductOwnerAndSale(product, newOwner, sale);
    }
  }

  private List<ProductPriceDiscount> getProductsFromSaleRequestDto(SaleRequestDto saleRequestDto) {
    if (saleRequestDto.getProducts() != null) {
      return saleRequestDto.getProducts().stream()
          .map(
              productDto -> {
                ProductPriceDiscount productPriceDiscount = new ProductPriceDiscount();
                productPriceDiscount.setProduct(
                    productService.getProduct(productDto.getProductId()));
                productPriceDiscount.setDiscount(productDto.getDiscount());
                return productPriceDiscount;
              })
          .toList();
    }
    return new ArrayList<>();
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

  private ProductReturnResponseDto validateSaleAfterReturnProduct(
      Sale sale, Product productToReturn) {
    if (sale.getProducts().isEmpty() && sale.getResources().isEmpty()) {
      return productService.getProductReturnResponseDto(null, productToReturn);
    }
    return productService.getProductReturnResponseDto(
        saleMapper.mapEntityToResponseDto(sale), productToReturn);
  }

  private ResourceReturnResponseDto validateSaleAfterReturnResource(
      Sale sale, PurchasedResourceInUser resourceToReturn) {
    if (sale.getResources().isEmpty() && sale.getProducts().isEmpty()) {
      sale = null;
    }
    return saleMapper.mapToResourceReturnResponseDto(
        resourceToReturn, saleMapper.mapEntityToResponseDto(sale));
  }

  private void setFieldsOfResourcesAfterSale(Sale sale) {
    List<PurchasedResourceInUser> resources = sale.getResources();
    resources.forEach(
        resource -> {
          resource.setPartOfSale(sale);
          resource.setOwner(sale.getBuyer());
          resource.setSalePrice(
              resource.getQuantity().multiply(resource.getResource().getPricePerQuantity()));
        });
    purchasedResourceInUserRepository.saveAll(resources);
  }

  private void removeQuantityFromResourcesInUser(Sale sale) {
    for (PurchasedResourceInUser resource : sale.getResources()) {
      ResourceInUser resourceInUser =
          resourceInUserService.getResourceInUser(sale.getSeller(), resource.getResource());
      resourceInUserService.removeQuantityFromResource(resourceInUser, resource.getQuantity());
    }
  }

  private PurchasedResourceInUser getPurchasedResource(UUID resourceId, UUID saleId) {
    return purchasedResourceInUserRepository
        .findByResourceIdAndPartOfSaleId(resourceId, saleId)
        .orElseThrow(() -> new ResourceNotFoundInSaleException(resourceId, saleId));
  }

  private void returnResourceFromSaleToUser(Sale sale, PurchasedResourceInUser resourceToReturn) {
    ResourceInUser resourceInUser =
        resourceInUserService.getResourceInUser(sale.getSeller(), resourceToReturn.getResource());
    resourceInUser.setQuantity(resourceInUser.getQuantity().add(resourceToReturn.getQuantity()));
  }

  private void throwExceptionIfResourceIsNotOwned(SaleRequestDto saleRequestDto) {
    if (saleRequestDto.getResources() != null) {
      for (PurchasedResourceQuantityRequestDto resource : saleRequestDto.getResources()) {
        resourceInUserService.findResourceInUserOrThrow(
            userService.getUser(saleRequestDto.getSellerId()),
            resource.getResourceAndQuantity().getResourceId());
      }
    }
  }

  private List<PurchasedResourceInUser> getResourcesFromSaleRequestDto(
      SaleRequestDto saleRequestDto) {
    List<PurchasedResourceInUser> resources = new ArrayList<>();
    if (saleRequestDto.getResources() != null) {
      for (PurchasedResourceQuantityRequestDto resourceRequest : saleRequestDto.getResources()) {
        PurchasedResourceInUser purchasedResourceInUser =
            getPurchasedResourceInUser(resourceRequest);
        resources.add(purchasedResourceInUser);
      }
    }
    return resources;
  }

  private PurchasedResourceInUser getPurchasedResourceInUser(
      PurchasedResourceQuantityRequestDto resourceRequest) {
    PurchasedResourceInUser purchasedResourceInUser = new PurchasedResourceInUser();
    Resource resource =
        resourceService.getResourceById(resourceRequest.getResourceAndQuantity().getResourceId());
    purchasedResourceInUser.setResource(resource);
    purchasedResourceInUser.setSalePrice(
        resource
            .getPricePerQuantity()
            .multiply(resourceRequest.getResourceAndQuantity().getQuantity()));
    purchasedResourceInUser.setDiscount(resourceRequest.getDiscount());
    purchasedResourceInUser.setQuantity(resourceRequest.getResourceAndQuantity().getQuantity());
    return purchasedResourceInUser;
  }

  private void setProductPriceDiscountSalePriceAndSale(Sale sale) {
    sale.getProducts()
        .forEach(
            productDto -> {
              Product product = productDto.getProduct();
              BigDecimal salePrice = productService.getProductSalePrice(product);
              productDto.setSalePrice(salePrice);
              productDto.setSale(sale);
            });
  }

  private void throwExceptionIfSellerNotProductOwner(
      List<ProductPriceDiscount> products, UUID sellerId) {
    for (ProductPriceDiscount productPriceDiscount : products) {
      Product product = productPriceDiscount.getProduct();
      if (!product.getOwner().getId().equals(sellerId)) {
        throw new UserNotOwnerException(product.getOwner().getId(), sellerId);
      }
    }
  }

  private static void throwExceptionIfNoResourcesAndProductsInRequest(
      SaleRequestDto saleRequestDto) {
    if ((saleRequestDto.getProducts() == null || saleRequestDto.getProducts().isEmpty())
        && (saleRequestDto.getResources() == null || saleRequestDto.getResources().isEmpty())) {
      throw new EmptySaleException();
    }
  }
}
