package jewellery.inventory.aspect;

import java.util.UUID;

public interface EntityFetcher {
  Object fetchEntity(Object... ids);
}
