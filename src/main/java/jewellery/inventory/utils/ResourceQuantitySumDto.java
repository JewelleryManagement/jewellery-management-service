package jewellery.inventory.utils;

import java.math.BigDecimal;
import java.util.UUID;

public record ResourceQuantitySumDto(UUID resourceId, BigDecimal quantity) {}
