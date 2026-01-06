package jewellery.inventory.service;

import jewellery.inventory.mapper.SaleMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.repository.PurchasedResourceInUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleService {
  private final PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  private final SaleMapper saleMapper;
  private final ProductService productService;
  private final UserService userService;
  private final ResourceService resourceService;
}
