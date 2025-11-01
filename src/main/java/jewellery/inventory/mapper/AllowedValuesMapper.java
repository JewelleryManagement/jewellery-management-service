package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.resource.AllowedValueResponseDto;
import jewellery.inventory.model.resource.AllowedValue;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AllowedValuesMapper {

  public AllowedValueResponseDto toDto(AllowedValue allowedValue) {
    return new AllowedValueResponseDto(
        allowedValue.getId().getResourceClazz(),
        allowedValue.getId().getFieldName(),
        allowedValue.getId().getValue());
  }
}
