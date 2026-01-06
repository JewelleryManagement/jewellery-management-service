package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.mapper.PurchasedResourceInUserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.PurchasedResourceInUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceInUserService {
  private final PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  private final PurchasedResourceInUserMapper purchasedResourceInUserMapper;
  private final UserService userService;

  public List<PurchasedResourceQuantityResponseDto> getAllPurchasedResources(UUID userId) {
    User user = userService.getUser(userId);
    return purchasedResourceInUserRepository.findAllByOwnerId(user.getId()).stream()
        .map(purchasedResourceInUserMapper::toPurchasedResourceQuantityResponseDto)
        .toList();
  }
}
