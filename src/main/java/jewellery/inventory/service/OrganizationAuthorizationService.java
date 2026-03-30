package jewellery.inventory.service;

import java.util.UUID;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.Permission;
import jewellery.inventory.repository.OrganizationMembershipRepository;
import jewellery.inventory.service.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("orgAuth")
@RequiredArgsConstructor
public class OrganizationAuthorizationService {

  private final OrganizationMembershipRepository membershipRepository;
  private final AuthService authService;

  @Transactional(readOnly = true)
  public boolean hasPermission(UUID organizationId, String permission) {
    UserResponseDto currentUser = authService.getCurrentUser();

    Permission permissionEnum = Permission.valueOf(permission);

    return membershipRepository
        .findByUserIdAndOrganizationId(currentUser.getId(), organizationId)
        .map(
            membership ->
                membership.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .anyMatch(p -> p == permissionEnum))
        .orElse(false);
  }

  @Transactional(readOnly = true)
  public boolean hasPermissionForProduct(UUID productId, String permission) {
    UUID currentUserId = authService.getCurrentUser().getId();

    Permission permissionEnum;
    try {
      permissionEnum = Permission.valueOf(permission);
    } catch (IllegalArgumentException e) {
      return false;
    }

    return membershipRepository.hasAccessToProduct(productId, currentUserId, permissionEnum);
  }

  @Transactional(readOnly = true)
  public boolean hasPermissionForSale(UUID saleId, String permission) {

    UUID currentUserId = authService.getCurrentUser().getId();

    Permission permissionEnum;
    try {
      permissionEnum = Permission.valueOf(permission);
    } catch (IllegalArgumentException e) {
      return false;
    }

    return membershipRepository.hasAccessToSale(saleId, currentUserId, permissionEnum);
  }
}
