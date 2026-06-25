package jewellery.inventory.model;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
  ORGANIZATION_READ(PermissionScope.ORGANIZATION, "organization:read"),
  ORGANIZATION_DELETE(PermissionScope.ORGANIZATION, "organization:delete"),

  ORGANIZATION_USER_ADD(PermissionScope.ORGANIZATION, "organization:user:add"),
  ORGANIZATION_USER_DELETE(PermissionScope.ORGANIZATION, "organization:user:delete"),
  ORGANIZATION_USER_READ(PermissionScope.ORGANIZATION, "organization:user:read"),
  ORGANIZATION_USER_ROLES_READ(PermissionScope.ORGANIZATION, "organization:user:roles:read"),

  ORGANIZATION_RESOURCE_ADD(PermissionScope.ORGANIZATION, "organization:resource:add"),
  ORGANIZATION_RESOURCE_DELETE(PermissionScope.ORGANIZATION, "organization:resource:delete"),
  ORGANIZATION_RESOURCE_READ(PermissionScope.ORGANIZATION, "organization:resource:read"),
  ORGANIZATION_RESOURCE_TRANSFER(PermissionScope.ORGANIZATION, "organization:resource:transfer"),

  ORGANIZATION_PRODUCT_CREATE(PermissionScope.ORGANIZATION, "organization:product:create"),
  ORGANIZATION_PRODUCT_UPDATE(PermissionScope.ORGANIZATION, "organization:product:update"),
  ORGANIZATION_PRODUCT_DELETE(PermissionScope.ORGANIZATION, "organization:product:delete"),
  ORGANIZATION_PRODUCT_TRANSFER(PermissionScope.ORGANIZATION, "organization:product:transfer"),
  ORGANIZATION_PRODUCT_READ(PermissionScope.ORGANIZATION, "organization:product:read"),

  ORGANIZATION_SALE_CREATE(PermissionScope.ORGANIZATION, "organization:sale:create"),
  ORGANIZATION_SALE_PRODUCT_RETURN(
      PermissionScope.ORGANIZATION, "organization:sale:product:return"),
  ORGANIZATION_SALE_READ(PermissionScope.ORGANIZATION, "organization:sale:read"),
  ORGANIZATION_SALE_RESOURCE_RETURN(
      PermissionScope.ORGANIZATION, "organization:sale:resource:return"),

  ORGANIZATION_ROLE_ASSIGN(PermissionScope.ORGANIZATION, "organization:role:assign"),
  ORGANIZATION_ROLE_READ(PermissionScope.ORGANIZATION, "organization:role:read"),
  ORGANIZATION_ROLE_UPDATE(PermissionScope.ORGANIZATION, "organization:role:update"),

  ORGANIZATION_EVENT_READ(PermissionScope.ORGANIZATION, "organization:event:read"),

  SYSTEM_USER_READ(PermissionScope.SYSTEM, "system:user:read"),
  SYSTEM_USER_CREATE(PermissionScope.SYSTEM, "system:user:create"),
  SYSTEM_USER_UPDATE(PermissionScope.SYSTEM, "system:user:update"),
  SYSTEM_USER_DELETE(PermissionScope.SYSTEM, "system:user:delete"),

  SYSTEM_ROLE_CREATE(PermissionScope.SYSTEM, "system:role:create"),
  SYSTEM_ROLE_DELETE(PermissionScope.SYSTEM, "system:role:delete"),
  SYSTEM_ROLE_READ(PermissionScope.SYSTEM, "system:role:read"),
  SYSTEM_ROLE_ASSIGN(PermissionScope.SYSTEM, "system:role:assign"),

  SYSTEM_RESOURCE_READ(PermissionScope.SYSTEM, "system:resource:read"),
  SYSTEM_RESOURCE_CREATE(PermissionScope.SYSTEM, "system:resource:create"),
  SYSTEM_RESOURCE_UPDATE(PermissionScope.SYSTEM, "system:resource:update"),
  SYSTEM_RESOURCE_DELETE(PermissionScope.SYSTEM, "system:resource:delete"),
  SYSTEM_RESOURCE_IMPORT(PermissionScope.SYSTEM, "system:resource:import"),

  SYSTEM_EVENT_READ(PermissionScope.SYSTEM, "system:event:read"),

  SYSTEM_ORGANIZATION_CREATE(PermissionScope.SYSTEM, "system:organization:create");

  public static Permission fromValue(String value) {
    for (Permission permission : values()) {
      if (permission.getValue().equals(value)) {
        return permission;
      }
    }
    throw new IllegalArgumentException("Unknown permission value: " + value);
  }

  private final PermissionScope permissionScope;
  private final String value;

  private final Set<Permission> implied = new HashSet<>();

  static {
    ORGANIZATION_DELETE.implied.add(ORGANIZATION_READ);

    ORGANIZATION_USER_ADD.implied.addAll(Set.of(ORGANIZATION_READ, ORGANIZATION_USER_READ));
    ORGANIZATION_USER_DELETE.implied.addAll(Set.of(ORGANIZATION_READ, ORGANIZATION_USER_READ));
    ORGANIZATION_USER_READ.implied.add(ORGANIZATION_READ);
    ORGANIZATION_USER_ROLES_READ.implied.addAll(Set.of(ORGANIZATION_READ, ORGANIZATION_USER_READ));

    ORGANIZATION_RESOURCE_ADD.implied.addAll(Set.of(ORGANIZATION_READ, ORGANIZATION_RESOURCE_READ));
    ORGANIZATION_RESOURCE_DELETE.implied.addAll(
        Set.of(ORGANIZATION_READ, ORGANIZATION_RESOURCE_READ));
    ORGANIZATION_RESOURCE_READ.implied.add(ORGANIZATION_READ);
    ORGANIZATION_RESOURCE_TRANSFER.implied.addAll(
        Set.of(ORGANIZATION_READ, ORGANIZATION_RESOURCE_READ));

    ORGANIZATION_PRODUCT_CREATE.implied.addAll(
        Set.of(
            ORGANIZATION_READ,
            ORGANIZATION_USER_READ,
            ORGANIZATION_RESOURCE_READ,
            ORGANIZATION_PRODUCT_READ));
    ORGANIZATION_PRODUCT_UPDATE.implied.addAll(
        Set.of(
            ORGANIZATION_READ,
            ORGANIZATION_USER_READ,
            ORGANIZATION_RESOURCE_READ,
            ORGANIZATION_PRODUCT_READ));
    ORGANIZATION_PRODUCT_DELETE.implied.addAll(
        Set.of(ORGANIZATION_READ, ORGANIZATION_PRODUCT_READ));
    ORGANIZATION_PRODUCT_TRANSFER.implied.addAll(
        Set.of(ORGANIZATION_READ, ORGANIZATION_PRODUCT_READ));
    ORGANIZATION_PRODUCT_READ.implied.add(ORGANIZATION_READ);

    ORGANIZATION_SALE_CREATE.implied.addAll(
        Set.of(
            ORGANIZATION_READ,
            ORGANIZATION_USER_READ,
            ORGANIZATION_PRODUCT_READ,
            ORGANIZATION_RESOURCE_READ));
    ORGANIZATION_SALE_PRODUCT_RETURN.implied.addAll(
        Set.of(ORGANIZATION_READ, ORGANIZATION_SALE_READ, ORGANIZATION_PRODUCT_READ));
    ORGANIZATION_SALE_READ.implied.addAll(
        Set.of(
            ORGANIZATION_READ,
            ORGANIZATION_RESOURCE_READ,
            ORGANIZATION_USER_READ,
            ORGANIZATION_PRODUCT_READ));
    ORGANIZATION_SALE_RESOURCE_RETURN.implied.addAll(
        Set.of(ORGANIZATION_READ, ORGANIZATION_SALE_READ, ORGANIZATION_RESOURCE_READ));

    SYSTEM_USER_CREATE.implied.add(SYSTEM_USER_READ);
    SYSTEM_USER_UPDATE.implied.add(SYSTEM_USER_READ);
    SYSTEM_USER_DELETE.implied.add(SYSTEM_USER_READ);

    SYSTEM_ROLE_CREATE.implied.add(SYSTEM_ROLE_READ);
    SYSTEM_ROLE_DELETE.implied.add(SYSTEM_ROLE_READ);
    SYSTEM_ROLE_ASSIGN.implied.add(SYSTEM_ROLE_READ);

    SYSTEM_RESOURCE_CREATE.implied.add(SYSTEM_RESOURCE_READ);
    SYSTEM_RESOURCE_UPDATE.implied.add(SYSTEM_RESOURCE_READ);
    SYSTEM_RESOURCE_DELETE.implied.add(SYSTEM_RESOURCE_READ);
    SYSTEM_RESOURCE_IMPORT.implied.add(SYSTEM_RESOURCE_READ);
  }

  public static Set<Permission> resolveAll(Set<Permission> selectedPermissions) {
    Set<Permission> resolved = EnumSet.noneOf(Permission.class);

    for (Permission permission : selectedPermissions) {
      collect(permission, resolved);
    }

    return resolved;
  }

  public Set<Permission> resolveIncludedPermissions() {
    Set<Permission> resolved = EnumSet.noneOf(Permission.class);
    collect(this, resolved);

    resolved.remove(this);
    return resolved;
  }

  private static void collect(Permission permission, Set<Permission> resolved) {
    if (!resolved.add(permission)) {
      return;
    }

    for (Permission impliedPermission : permission.implied) {
      collect(impliedPermission, resolved);
    }
  }
}
