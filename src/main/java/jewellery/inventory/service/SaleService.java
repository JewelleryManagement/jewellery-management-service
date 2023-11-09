package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.exception.product.ProductOwnerNotSeller;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.mapper.UserMapper;
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
  private final UserMapper userMapper;
  private final UserService userService;

  public List<SaleResponseDto> getAllSales() {
    List<Sale> sales = saleRepository.findAll();
    return sales.stream().map(saleMapper::mapEntityToResponseDto).toList();
  }

  public SaleResponseDto createSale(SaleRequestDto saleRequestDto) {
    Sale sale =
        saleMapper.mapRequestToEntity(
            saleRequestDto,
            userMapper.toUserEntity(userService.getUser(saleRequestDto.getSellerId())),
            userMapper.toUserEntity(userService.getUser(saleRequestDto.getBuyerId())),
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
      productService.updateProductOwner(product,userMapper.toUserEntity(userService.getUser(buyerId)),sale);
    }
  }

  private List<Product> getProductsFromSaleRequestDto(SaleRequestDto saleRequestDto) {
    return saleRequestDto.getProducts().stream()
        .map(
            productPriceDiscountRequestDto ->
                productService.returnProductByID(productPriceDiscountRequestDto.getProductId())).toList();
  }
}
