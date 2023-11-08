package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.not_found.ProductNotFoundException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.exception.product.ProductOwnerNotSeller;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.repository.SaleRepository;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleService {
  private final SaleRepository saleRepository;
  private final SaleMapper saleMapper;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  public List<SaleResponseDto> getAllSales() {
    List<Sale> sales = saleRepository.findAll();
    return sales.stream().map(saleMapper::mapEntityToResponseDto).toList();
  }

  public SaleResponseDto createSale(SaleRequestDto saleRequestDto) {
    Sale sale =
        saleMapper.mapRequestToEntity(
            saleRequestDto,
            getUserById(saleRequestDto.getSellerId()),
            getUserById(saleRequestDto.getBuyerId()),
            getProductsFromSaleRequestDto(saleRequestDto));

    throwExceptionIfProductOwnerNotSeller(sale.getProducts(), saleRequestDto.getSellerId());
    Sale createdSale = saleRepository.save(sale);
    updateProductOwnersAndSale(sale.getProducts(), saleRequestDto.getBuyerId(), createdSale);
    return saleMapper.mapEntityToResponseDto(createdSale);
  }

  private void throwExceptionIfProductOwnerNotSeller(List<Product> products, UUID sellerId) {
    for (Product product : products) {
      if (!product.getOwner().getId().equals(sellerId)) {
        throw new ProductOwnerNotSeller(product.getOwner().getId(), sellerId);
      }
    }
  }

  private void updateProductOwnersAndSale(List<Product> products, UUID buyerId, Sale sale) {
    for (Product product : products) {
      product.setOwner(userRepository.getReferenceById(buyerId));
      product.setPartOfSale(sale);
      productRepository.save(product);
    }
  }

  private List<Product> getProductsFromSaleRequestDto(SaleRequestDto saleRequestDto) {
    return saleRequestDto.getProducts().stream()
        .map(
            productPriceDiscountRequestDto ->
                productRepository
                    .findById(productPriceDiscountRequestDto.getProductId())
                    .orElseThrow(
                        () ->
                            new ProductNotFoundException(
                                productPriceDiscountRequestDto.getProductId())))
        .toList();
  }

  private User getUserById(UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
  }
}
