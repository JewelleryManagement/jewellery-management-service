package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.exception.product.UserNotOwnerException;
import jewellery.inventory.mapper.SaleMapper;
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

  public SaleResponseDto createSale(SaleRequestDto saleRequestDto) {
    Sale sale =
        saleMapper.mapRequestToEntity(
            saleRequestDto,
            userService.getUser(saleRequestDto.getSellerId()),
            userService.getUser(saleRequestDto.getBuyerId()),
            getProductsFromSaleRequestDto(saleRequestDto));

    throwExceptionIfProductIsSold(sale.getProducts());
    throwExceptionIfUserNotSeller(sale.getProducts(), saleRequestDto.getSellerId());
    throwExceptionIfProductIsPartOfAnotherProduct(sale.getProducts());

    Sale createdSale = saleRepository.save(sale);
    updateProductOwnersAndSale(sale.getProducts(), saleRequestDto.getBuyerId(), createdSale);
    return saleMapper.mapEntityToResponseDto(createdSale);
  }

  private void throwExceptionIfUserNotSeller(List<Product> products, UUID sellerId) {
    for (Product product : products) {
      if (!product.getOwner().getId().equals(sellerId)) {
        throw new UserNotOwnerException(product.getOwner().getId(), sellerId);
      }
    }
  }

  private void throwExceptionIfProductIsPartOfAnotherProduct(List<Product> products) {
    for (Product product : products) {
      if (product.getContentOf() != null) {
        throw new ProductIsContentException(product.getId());
      }
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
      updateOwnerAndSubProducts(product, newOwner, sale);
    }
  }

  private void updateOwnerAndSubProducts(Product product, User newOwner, Sale sale) {
    productService.updateProductOwnerAndSale(product, newOwner, sale);

    List<Product> subProducts = product.getProductsContent();
    for (Product subProduct : subProducts) {
      updateOwnerAndSubProducts(subProduct, newOwner, sale);
    }
  }

  private List<Product> getProductsFromSaleRequestDto(SaleRequestDto saleRequestDto) {
    return saleRequestDto.getProducts().stream()
        .map(
            productPriceDiscountRequestDto ->
                productService.getProduct(productPriceDiscountRequestDto.getProductId()))
        .toList();
  }
}
