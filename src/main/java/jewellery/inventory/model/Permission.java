package jewellery.inventory.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
  ORGANIZATION_READ("organization:read"),
  ORGANIZATION_USER_ADD("organization:user:add"),
  ORGANIZATION_USER_DELETE("organization:user:delete"),
  ORGANIZATION_DELETE("organization:delete"),
  ORGANIZATION_PERMISSION_UPDATE("organization:permission:update"),
  ORGANIZATION_USER_READ("organization:user:read"),

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
  ORGANIZATION_USER_ROLES_READ("organization:user:roles:read"),

  ORGANIZATION_SMTH("SMTH");

  private final String value;
}
