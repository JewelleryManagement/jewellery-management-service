package jewellery.inventory.service;

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
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleService {
  private final SaleRepository saleRepository;
  private final SaleMapper saleMapper;
  private final ProductService productService;
  private final UserService userService;

  public List<SaleResponseDto> getAllSales() {
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
            getProductsFromSaleRequestDto(saleRequestDto));

    throwExceptionIfProductIsSold(sale.getProducts());
    throwExceptionIfSellerNotProductOwner(sale.getProducts(), saleRequestDto.getSellerId());
    throwExceptionIfProductIsPartOfAnotherProduct(sale.getProducts());

    Sale createdSale = saleRepository.save(sale);
    updateProductOwnersAndSale(sale.getProducts(), saleRequestDto.getBuyerId(), createdSale);
    return saleMapper.mapEntityToResponseDto(createdSale);
  }

  private void throwExceptionIfSellerNotProductOwner(List<Product> products, UUID sellerId) {
    for (Product product : products) {
      if (!product.getOwner().getId().equals(sellerId)) {
        throw new UserNotOwnerException(product.getOwner().getId(), sellerId);
      }
    }
  }

  public ProductReturnResponseDto returnProduct(UUID productId) {
    Product productToReturn = productService.getProduct(productId);

    throwExceptionIfProductIsPartOfAnotherProduct(productToReturn);
    throwExceptionIfProductNotSold(productToReturn);

    Sale sale = getSale(productToReturn.getPartOfSale().getId());
    sale.setProducts(removeProductFromSale(sale.getProducts(), productToReturn));

    productService.updateProductOwnerAndSale(productToReturn, sale.getSeller(), null);

    deleteSaleIfProductsIsEmpty(sale);
    return validateSaleAfterReturnProduct(sale, productToReturn);
  }

  private Sale getSale(UUID saleId) {
    return saleRepository.findById(saleId).orElseThrow(() -> new SaleNotFoundException(saleId));
  }

  private void throwExceptionIfProductIsPartOfAnotherProduct(List<Product> products) {
    for (Product product : products) {
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

  private void throwExceptionIfProductIsSold(List<Product> products) {
    for (Product product : products) {
      if (product.getPartOfSale() != null) {
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
    return saleRequestDto.getProducts().stream()
        .map(
            productPriceDiscountRequestDto ->
                productService.getProduct(productPriceDiscountRequestDto.getProductId()))
        .toList();
  }

  private void deleteSaleIfProductsIsEmpty(Sale sale) {
    if (sale.getProducts().isEmpty()) {
      saleRepository.deleteById(sale.getId());
    } else saleRepository.save(sale);
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
    products.remove(productToRemove);
    return products;
  }
}
