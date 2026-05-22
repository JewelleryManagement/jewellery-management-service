package jewellery.inventory.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.exception.not_found.ResourceNotFoundInSaleException;
import jewellery.inventory.mapper.PurchasedResourceInUserMapper;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.PurchasedResourceInUser;
import jewellery.inventory.repository.PurchasedResourceInUserRepository;
import jewellery.inventory.service.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchasedResourceInUserService {
  private final PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  private final PurchasedResourceInUserMapper purchasedResourceInUserMapper;
  private final AuthService authService;

  public List<PurchasedResourceQuantityResponseDto> getAllPurchasedResources(UUID userId) {
    UUID currentUserId = authService.getCurrentUser().getId();

    Set<Permission> permissions =
        Set.of(Permission.ORGANIZATION_RESOURCE_READ, Permission.ORGANIZATION_SALE_READ);

    return purchasedResourceInUserRepository
        .findAllByOwnerIdAndAllPermissions(userId, currentUserId, permissions, permissions.size())
        .stream()
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
