package jewellery.inventory.exception;

import java.util.UUID;

public class EntityFetchException extends RuntimeException {

  public EntityFetchException(String methodName, UUID entityId, Exception e) {
    super(
        String.format(
            "Failed to fetch entity with ID %s using method %s due to: %s",
            entityId.toString(), methodName, e.getMessage()),
        e);
  }
}
