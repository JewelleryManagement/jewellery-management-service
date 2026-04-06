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
  public boolean hasOrganizationPermission(UUID organizationId, String permission) {
    UserResponseDto currentUser = authService.getCurrentUser();

    Permission permissionValue = Permission.valueOf(permission);

    return membershipRepository.hasPermissionInOrganization(
        currentUser.getId(), organizationId, permissionValue);
  }

  @Transactional(readOnly = true)
  public boolean hasPermissionForProduct(UUID productId, String permission) {
    UUID currentUserId = authService.getCurrentUser().getId();

    Permission permissionValue = Permission.valueOf(permission);

    return membershipRepository.hasAccessToProduct(productId, currentUserId, permissionValue);
  }

  @Transactional(readOnly = true)
  public boolean hasPermissionForSale(UUID saleId, String permission) {
    UUID currentUserId = authService.getCurrentUser().getId();

    Permission permissionValue = Permission.valueOf(permission);

    return membershipRepository.hasAccessToSale(saleId, currentUserId, permissionValue);
  }
}
