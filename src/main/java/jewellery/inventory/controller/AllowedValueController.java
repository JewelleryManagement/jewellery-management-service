package jewellery.inventory.controller;

import java.util.List;
import jewellery.inventory.dto.request.resource.AllowedValueRequestDto;
import jewellery.inventory.dto.response.resource.AllowedValueResponseDto;
import jewellery.inventory.model.resource.AllowedValue;
import jewellery.inventory.service.AllowedValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/allowed-values")
@RequiredArgsConstructor
public class AllowedValueController {
  private final AllowedValueService allowedValueService;

  @PostMapping
  public ResponseEntity<AllowedValue> addAllowedValue(@RequestBody AllowedValueRequestDto dto) {
    AllowedValue.AllowedValueId id =
        AllowedValue.AllowedValueId.builder()
            .resourceClazz(dto.getResourceClazz())
            .fieldName(dto.getFieldName())
            .value(dto.getValue())
            .sku(dto.getSku())
            .build();
    AllowedValue allowedValue = AllowedValue.builder().id(id).build();
    return ResponseEntity.ok(allowedValueService.addAllowedValue(allowedValue));
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteAllowedValue(@RequestBody AllowedValueRequestDto dto) {
    AllowedValue.AllowedValueId id =
        AllowedValue.AllowedValueId.builder()
            .resourceClazz(dto.getResourceClazz())
            .fieldName(dto.getFieldName())
            .value(dto.getValue())
            .sku(dto.getSku())
            .build();
    allowedValueService.deleteAllowedValue(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<AllowedValueResponseDto>> getAllowedValues(
      @RequestParam String resourceClazz, @RequestParam String fieldName) {
    return ResponseEntity.ok(allowedValueService.getAllowedValueDtos(resourceClazz, fieldName));
  }
}
