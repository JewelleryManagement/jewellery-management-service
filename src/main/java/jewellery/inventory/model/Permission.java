package jewellery.inventory.model;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
  ORGANIZATION_READ("organization:read"),
  ORGANIZATION_DELETE("organization:delete"),

  ORGANIZATION_USER_ADD("organization:user:add"),
  ORGANIZATION_USER_DELETE("organization:user:delete"),
  ORGANIZATION_USER_READ("organization:user:read"),
  ORGANIZATION_USER_ROLES_READ("organization:user:roles:read"),

  ORGANIZATION_RESOURCE_ADD("organization:resource:add"),
  ORGANIZATION_RESOURCE_DELETE("organization:resource:delete"),
  ORGANIZATION_RESOURCE_READ("organization:resource:read"),
  ORGANIZATION_RESOURCE_TRANSFER("organization:resource:transfer"),

  ORGANIZATION_PRODUCT_CREATE("organization:product:create"),
  ORGANIZATION_PRODUCT_UPDATE("organization:product:update"),
  ORGANIZATION_PRODUCT_DELETE("organization:product:delete"),
  ORGANIZATION_PRODUCT_TRANSFER("organization:product:transfer"),
  ORGANIZATION_PRODUCT_READ("organization:product:read"),

  ORGANIZATION_SALE_CREATE("organization:sale:create"),
  ORGANIZATION_SALE_PRODUCT_RETURN("organization:sale:product:return"),
  ORGANIZATION_SALE_READ("organization:sale:read"),
  ORGANIZATION_SALE_RESOURCE_RETURN("organization:sale:resource:return"),

  ORGANIZATION_ROLE_ASSIGN("organization:role:assign"),
  ORGANIZATION_ROLE_READ("organization:role:read"),
  ORGANIZATION_ROLE_UPDATE("organization:role:update"),

  ORGANIZATION_EVENT_READ("organization:event:read");

  public static Permission fromValue(String value) {
    for (Permission permission : values()) {
      if (permission.getValue().equals(value)) {
        return permission;
      }
    }
    throw new IllegalArgumentException("Unknown permission value: " + value);
  }

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
