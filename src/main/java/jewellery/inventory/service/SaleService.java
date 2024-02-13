package jewellery.inventory.service;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.not_found.SaleNotFoundException;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.ProductNotSoldException;
import jewellery.inventory.exception.product.UserNotOwnerException;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.*;
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

  public List<SaleResponseDto> getAllSales() {
    logger.debug("Fetching all Sales");
    List<Sale> sales = saleRepository.findAll();
    return sales.stream().map(saleMapper::mapEntityToResponseDto).toList();
  }

  @LogCreateEvent(eventType = EventType.SALE_CREATE)
  @Transactional
  public SaleResponseDto createSale(SaleRequestDto saleRequestDto) {
    Sale sale =
        saleMapper.mapRequestToEntity(
            saleRequestDto,
            userService.getUser(saleRequestDto.getSellerId()),
            userService.getUser(saleRequestDto.getBuyerId()),
            getProductsFromSaleRequestDto(saleRequestDto));

    throwExceptionIfProductIsSold(sale.getProducts());
    throwExceptionIfSellerNotProductOwner(sale.getProducts(), saleRequestDto.getSellerId());
    throwExceptionIfProductIsPartOfAnotherProduct(sale.getProducts());

    Sale createdSale = saleRepository.save(sale);
    setProductPriceDiscountSalePriceAndSale(createdSale);
    updateProductOwnersAndSale(sale.getProducts(), saleRequestDto.getBuyerId(), createdSale);
    logger.info("Sale created successfully. Sale ID: {}", createdSale.getId());
    return saleMapper.mapEntityToResponseDto(createdSale);
  }

  private void setProductPriceDiscountSalePriceAndSale(Sale sale) {
    sale.getProducts().forEach(productDto -> {
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
    deleteSaleIfProductsIsEmpty(sale);
    logger.info("Product returned successfully. Product ID: {}", productId);
    return validateSaleAfterReturnProduct(sale, productToReturn);
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
    return saleRequestDto.getProducts().stream()
        .map(
            productDto -> {
              ProductPriceDiscount productPriceDiscount = new ProductPriceDiscount();
              productPriceDiscount.setProduct(productService.getProduct(productDto.getProductId()));
              productPriceDiscount.setDiscount(productDto.getDiscount());
              return productPriceDiscount;
            })
        .toList();
  }

  private void deleteSaleIfProductsIsEmpty(Sale sale) {
    if (sale.getProducts().isEmpty()) {
      logger.info("Deleting sale with ID: {} since the products list is empty.", sale.getId());
      saleRepository.deleteById(sale.getId());
    } else {
      logger.info("Saving sale with ID: {} since the products list is not empty.", sale.getId());
      saleRepository.save(sale);
    }
  }

  private ProductReturnResponseDto validateSaleAfterReturnProduct(
      Sale sale, Product productToReturn) {
    if (sale.getProducts().isEmpty()) {
      return productService.getProductReturnResponseDto(null, productToReturn);
    }
    return productService.getProductReturnResponseDto(
        saleMapper.mapEntityToResponseDto(sale), productToReturn);
  }
}
