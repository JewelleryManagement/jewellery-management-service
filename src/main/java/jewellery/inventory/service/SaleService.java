package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.dto.request.PurchasedResourceInUserRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.not_found.SaleNotFoundException;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.ProductNotSoldException;
import jewellery.inventory.exception.product.UserNotOwnerException;
import jewellery.inventory.exception.resources.ResourceIsPartOfProductException;
import jewellery.inventory.exception.resources.ResourceSoldException;
import jewellery.inventory.mapper.PurchasedResourceInUserMapper;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
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
  private final SaleMapper saleMapper;
  private final ProductService productService;
  private final UserService userService;
  private final PurchasedResourceInUserMapper purchasedResourceInUserMapper;

  public List<SaleResponseDto> getAllSales() {
    logger.debug("Fetching all Sales");
    List<Sale> sales = saleRepository.findAll();
    return sales.stream().map(saleMapper::mapEntityToResponseDto).toList();
  }

  @LogCreateEvent(eventType = EventType.SALE_CREATE)
  public SaleResponseDto createSale(SaleRequestDto saleRequestDto) {
    Sale sale =
        saleMapper.mapRequestToEntity(
            saleRequestDto,
            userService.getUser(saleRequestDto.getSellerId()),
            userService.getUser(saleRequestDto.getBuyerId()),
            getProductsFromSaleRequestDto(saleRequestDto),
            getResourcesFromSaleRequestDto(saleRequestDto));

    throwExceptionIfProductIsSold(sale.getProducts());
    throwExceptionIfResourceIsSold(sale.getResources());
    throwExceptionIfSellerNotProductOwner(sale.getProducts(), saleRequestDto.getSellerId());
    throwExceptionIfProductIsPartOfAnotherProduct(sale.getProducts());
    throwExceptionIfResourceIsPartOfProduct(sale.getSeller(), sale.getResources());

    Sale createdSale = saleRepository.save(sale);
    updateProductOwnersAndSale(sale.getProducts(), saleRequestDto.getBuyerId(), createdSale);
    logger.info("Sale created successfully. Sale ID: {}", createdSale.getId());
    return saleMapper.mapEntityToResponseDto(createdSale);
  }

  private void throwExceptionIfSellerNotProductOwner(List<Product> products, UUID sellerId) {
    for (Product product : products) {
      if (!product.getOwner().getId().equals(sellerId)) {
        logger.error(
            "Seller with ID {} is not the owner of product with ID {}", sellerId, product.getId());
        throw new UserNotOwnerException(product.getOwner().getId(), sellerId);
      }
    }
  }

  @LogCreateEvent(eventType = EventType.SALE_RETURN_PRODUCT)
  public ProductReturnResponseDto returnProduct(UUID productId) {
    Product productToReturn = productService.getProduct(productId);

    throwExceptionIfProductIsPartOfAnotherProduct(productToReturn);
    throwExceptionIfProductNotSold(productToReturn);

    Sale sale = getSale(productToReturn.getPartOfSale().getId());
    sale.setProducts(removeProductFromSale(sale.getProducts(), productToReturn));

    productService.updateProductOwnerAndSale(productToReturn, sale.getSeller(), null);

    deleteSaleIfProductsIsEmpty(sale);
    logger.info("Product returned successfully. Product ID: {}", productId);
    return validateSaleAfterReturnProduct(sale, productToReturn);
  }

  private Sale getSale(UUID saleId) {
    return saleRepository.findById(saleId).orElseThrow(() -> new SaleNotFoundException(saleId));
  }

  private void throwExceptionIfProductIsPartOfAnotherProduct(List<Product> products) {
    for (Product product : products) {
      if (product.getContentOf() != null) {
        logger.error("Product with ID {} is part of another product.", product.getId());
        throw new ProductIsContentException(product.getId());
      }
    }
  }

  private void throwExceptionIfProductIsPartOfAnotherProduct(Product product) {
    if (product.getContentOf() != null) {
      logger.error("Product with ID {} is part of another product.", product.getId());
      throw new ProductIsContentException(product.getId());
    }
  }

  private void throwExceptionIfProductNotSold(Product product) {
    if (product.getPartOfSale() == null) {
      logger.error("Product with ID {} is not sold.", product.getId());
      throw new ProductNotSoldException(product.getId());
    }
  }

  private void throwExceptionIfProductIsSold(List<Product> products) {
    for (Product product : products) {
      if (product.getPartOfSale() != null) {
        logger.error("Product with ID {} is already sold.", product.getId());
        throw new ProductIsSoldException(product.getId());
      }
    }
  }

  private void updateProductOwnersAndSale(List<Product> products, UUID buyerId, Sale sale) {
    User newOwner = userService.getUser(buyerId);
    for (Product product : products) {
      productService.updateProductOwnerAndSale(product, newOwner, sale);
    }
  }

  private List<Product> getProductsFromSaleRequestDto(SaleRequestDto saleRequestDto) {
    logger.info("Getting products from sale request.");
    return saleRequestDto.getProducts().stream()
        .map(
            productPriceDiscountRequestDto ->
                productService.getProduct(productPriceDiscountRequestDto.getProductId()))
        .toList();
  }

  private void deleteSaleIfProductsIsEmpty(Sale sale) {
    if (sale.getProducts().isEmpty()) {
      logger.info("Deleting sale with ID: {} since the products list is empty.", sale.getId());
      saleRepository.deleteById(sale.getId());
    } else saleRepository.save(sale);
    logger.info("Saving sale with ID: {} since the products list is not empty.", sale.getId());
  }

  private ProductReturnResponseDto validateSaleAfterReturnProduct(
      Sale sale, Product productToReturn) {
    if (sale.getProducts().isEmpty()) {
      return productService.getProductReturnResponseDto(null, productToReturn);
    }
    return productService.getProductReturnResponseDto(
        saleMapper.mapEntityToResponseDto(sale), productToReturn);
  }

  private List<Product> removeProductFromSale(List<Product> products, Product productToRemove) {
    logger.info("Removing product with ID: {} from sale.", productToRemove.getId());
    products.remove(productToRemove);
    return products;
  }

  private List<PurchasedResourceInUser> getResourcesFromSaleRequestDto(
      SaleRequestDto saleRequestDto) {
    logger.info("Getting resources from sale request.");
    List<PurchasedResourceInUserRequestDto> resources = saleRequestDto.getResources();
    return resources.stream()
        .map(purchasedResourceInUserMapper::toPurchasedResourceInUser)
        .toList();
  }

  private void throwExceptionIfResourceIsSold(List<PurchasedResourceInUser> resources) {
    resources.forEach(
        resource -> {
          if (resource.getPartOfSale() != null) {
            throw new ResourceSoldException(resource.getId());
          }
        });
  }

  private void throwExceptionIfResourceIsPartOfProduct(
      User owner, List<PurchasedResourceInUser> resources) {
    List<Product> products = owner.getProductsOwned();

    products.forEach(
        product -> {
          List<ResourceInProduct> resourcesInProduct = product.getResourcesContent();
          resourcesInProduct.forEach(
              res -> {
                Resource resource = res.getResource();
                resources.forEach(
                    purchasedResource -> {
                      if (purchasedResource.getResource().getId() == resource.getId()) {
                        throw new ResourceIsPartOfProductException(
                            resource.getId(), product.getId());
                      }
                    });
              });
        });
  }
}
