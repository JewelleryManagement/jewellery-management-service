package jewellery.inventory.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleType {
  ORGANIZATION(Set.of(PermissionScope.ORGANIZATION)),
  SYSTEM(Set.of(PermissionScope.SYSTEM));

  private final Set<PermissionScope> allowedPermissionScopes;

  public boolean allows(Permission permission) {
    return allowedPermissionScopes.contains(permission.getPermissionScope());
  }
}
