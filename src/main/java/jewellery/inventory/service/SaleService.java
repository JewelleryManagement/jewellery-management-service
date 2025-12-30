package jewellery.inventory.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.PurchasedResourceQuantityRequestDto;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.ResourceReturnResponseDto;
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
  private final PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  private final SaleMapper saleMapper;
  private final ProductService productService;
  private final UserService userService;
  private final ResourceService resourceService;

  public void updateProductOwnersAndSale(
      List<ProductPriceDiscount> products, UUID buyerId, Sale sale) {
    User newOwner = userService.getUser(buyerId);

    for (ProductPriceDiscount productPriceDiscount : products) {
      Product product = productPriceDiscount.getProduct();
      productService.updateProductOwnerAndSale(product, newOwner, sale);
    }
  }

  public List<ProductPriceDiscount> getProductsFromSaleRequestDto(SaleRequestDto saleRequestDto) {
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

  public ProductReturnResponseDto validateSaleAfterReturnProduct(
      Sale sale, Product productToReturn) {
    if (sale.getProducts().isEmpty() && sale.getResources().isEmpty()) {
      return productService.getProductReturnResponseDto(null, productToReturn);
    }
    return productService.getProductReturnResponseDto(
        saleMapper.mapEntityToResponseDto(sale), productToReturn);
  }

  public ResourceReturnResponseDto validateSaleAfterReturnResource(
      Sale sale, PurchasedResourceInUser resourceToReturn) {
    if (sale.getResources().isEmpty() && sale.getProducts().isEmpty()) {
      sale = null;
    }
    return saleMapper.mapToResourceReturnResponseDto(
        resourceToReturn, saleMapper.mapEntityToResponseDto(sale));
  }

  public void setFieldsOfResourcesAfterSale(Sale sale) {
    if (sale.getResources() != null) {
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
  }

  public PurchasedResourceInUser getPurchasedResource(UUID resourceId, UUID saleId) {
    return purchasedResourceInUserRepository
        .findByResourceIdAndPartOfSaleId(resourceId, saleId)
        .orElseThrow(() -> new ResourceNotFoundInSaleException(resourceId, saleId));
  }

  public List<PurchasedResourceInUser> getResourcesFromSaleRequestDto(
      SaleRequestDto saleRequestDto) {
    if (saleRequestDto.getResources() != null) {
      List<PurchasedResourceInUser> resources = new ArrayList<>();
      for (PurchasedResourceQuantityRequestDto resourceRequest : saleRequestDto.getResources()) {
        PurchasedResourceInUser purchasedResourceInUser =
            getPurchasedResourceInUser(resourceRequest);
        resources.add(purchasedResourceInUser);
      }

      return resources;
    }
    return new ArrayList<>();
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

  public void setProductPriceDiscountSalePriceAndSale(Sale sale) {
    sale.getProducts()
        .forEach(
            productDto -> {
              Product product = productDto.getProduct();
              BigDecimal salePrice = productService.getProductSalePrice(product);
              productDto.setSalePrice(salePrice);
              productDto.setSale(sale);
            });
  }

  public static void throwExceptionIfNoResourcesAndProductsInRequest(
      SaleRequestDto saleRequestDto) {
    if ((saleRequestDto.getProducts() == null || saleRequestDto.getProducts().isEmpty())
        && (saleRequestDto.getResources() == null || saleRequestDto.getResources().isEmpty())) {
      throw new EmptySaleException();
    }
  }
}
