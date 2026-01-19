package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.exception.not_found.ResourceNotFoundInSaleException;
import jewellery.inventory.mapper.PurchasedResourceInUserMapper;
import jewellery.inventory.model.PurchasedResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.PurchasedResourceInUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchasedResourceInUserService {
  private final PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  private final PurchasedResourceInUserMapper purchasedResourceInUserMapper;
  private final UserService userService;

  public List<PurchasedResourceQuantityResponseDto> getAllPurchasedResources(UUID userId) {
    User user = userService.getUser(userId);
    return purchasedResourceInUserRepository.findAllByOwnerId(user.getId()).stream()
        .map(purchasedResourceInUserMapper::toPurchasedResourceQuantityResponseDto)
        .toList();
  }

  public PurchasedResourceInUser getPurchasedResource(UUID resourceId, UUID saleId) {
    return purchasedResourceInUserRepository
        .findByResourceIdAndPartOfSaleId(resourceId, saleId)
        .orElseThrow(() -> new ResourceNotFoundInSaleException(resourceId, saleId));
  }

  public void saveAllPurchasedResources(List<PurchasedResourceInUser> resources) {
    purchasedResourceInUserRepository.saveAll(resources);
  }
}
