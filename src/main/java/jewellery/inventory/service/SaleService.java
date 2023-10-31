package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.product.ProductOwnerEqualsRecipientException;
import jewellery.inventory.exception.product.ProductOwnerNotSeller;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.Sale;
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
    Sale sale = saleMapper.mapRequestToEntity(saleRequestDto);
    throwExceptionIfProductOwnerNotSeller(sale.getProducts(), saleRequestDto.getSellerId());
    throwExceptionProductOwnerEqualsRecipientException(
        sale.getProducts(), saleRequestDto.getBuyerId());
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

  private void throwExceptionProductOwnerEqualsRecipientException(
      List<Product> products, UUID buyerId) {
    for (Product product : products) {
      if (product.getOwner().getId().equals(buyerId)) {
        throw new ProductOwnerEqualsRecipientException(buyerId);
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
}
