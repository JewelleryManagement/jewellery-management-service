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

    validateProductsOwnersAndContentsOf(sale.getProducts(), saleRequestDto.getSellerId());
    Sale createdSale = saleRepository.save(sale);
    updateProductOwnersAndSale(sale.getProducts(), saleRequestDto.getBuyerId(), createdSale);
    return saleMapper.mapEntityToResponseDto(createdSale);
  }

  private void validateProductsOwnersAndContentsOf(List<Product> products, UUID sellerId) {
    products.stream()
        .filter(product -> product.getPartOfSale() != null)
        .forEach(
            product -> {
              throw new ProductIsSoldException(product.getId());
            });
    products.stream()
        .filter(product -> product.getContentOf() != null)
        .forEach(
            product -> {
              throw new ProductIsContentException(product.getId());
            });
    products.stream()
        .filter(product -> !product.getOwner().getId().equals(sellerId))
        .forEach(
            product -> {
              throw new UserNotOwnerException(product.getOwner().getId(), sellerId);
            });
  }

  private void updateProductOwnersAndSale(List<Product> products, UUID buyerId, Sale sale) {
    for (Product product : products) {
      productService.updateProductOwnerAndSale(product, userService.getUser(buyerId), sale);
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
