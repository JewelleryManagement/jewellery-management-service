package jewellery.inventory.aspect.strategy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import jewellery.inventory.model.EventType;
import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;

@AllArgsConstructor
public abstract class PayloadStrategy<T, U> {

  public abstract Map<String, Object> createPayload(
      Object entity, @Nullable Object updatedEntity, EventType eventType);

  protected String formatCurrentTimestamp() {
    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'at' HH:mm:ss 'on' dd.MM.yyyy");
    return localDateTime.format(formatter);
  }
}
