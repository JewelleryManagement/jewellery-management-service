package jewellery.inventory.service;

import java.util.List;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.Sale;
import jewellery.inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleService {
  private final SaleRepository saleRepository;
  private final SaleMapper saleMapper;

  public List<SaleResponseDto> getAllSales() {
    List<Sale> sales = saleRepository.findAll();
    return sales.stream().map(saleMapper::mapToSaleResponseDto).toList();
  }
}
