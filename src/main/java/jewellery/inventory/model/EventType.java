package jewellery.inventory.model;

public enum EventType {
  USER_CREATE,
  USER_UPDATE,
  USER_DELETE,
  RESOURCE_CREATE,
  RESOURCE_DELETE,
  RESOURCE_UPDATE,
  RESOURCE_TRANSFER,
  RESOURCE_REMOVE_QUANTITY,
  RESOURCE_ADD_QUANTITY,
  PRODUCT_CREATE,
  PRODUCT_TRANSFER,
  PRODUCT_DISASSEMBLY,
  SALE_CREATE,
  SALE_RETURN_PRODUCT,
  PRODUCT_UPDATE,
  SALE_RETURN_RESOURCE,
  ORGANIZATION_CREATE,
  ORGANIZATION_DELETE,
  ORGANIZATION_USER_CREATE,
  ORGANIZATION_USER_DELETE,
  ORGANIZATION_USER_UPDATE,
  ORGANIZATION_ADD_RESOURCE_QUANTITY,
  ORGANIZATION_REMOVE_RESOURCE_QUANTITY,
  ORGANIZATION_PRODUCT_CREATE,
  ORGANIZATION_PRODUCT_UPDATE,
  ORGANIZATION_PRODUCT_DISASSEMBLY
}
