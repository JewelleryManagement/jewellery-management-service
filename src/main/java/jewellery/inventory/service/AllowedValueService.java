package jewellery.inventory.service;

import java.util.List;
import jewellery.inventory.dto.response.resource.AllowedValueResponseDto;
import jewellery.inventory.exception.not_found.NotFoundException;
import jewellery.inventory.mapper.AllowedValuesMapper;
import jewellery.inventory.model.resource.AllowedValue;
import jewellery.inventory.repository.AllowedValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllowedValueService {
  private final AllowedValueRepository allowedValueRepository;
  private final AllowedValuesMapper allowedValuesMapper;

  public AllowedValue addAllowedValue(AllowedValue allowedValue) {
    return allowedValueRepository.save(allowedValue);
  }

  public void deleteAllowedValue(AllowedValue.AllowedValueId id) {
    if (!allowedValueRepository.existsById(id)) {
      throw new NotFoundException("AllowedValue not found");
    }
    allowedValueRepository.deleteById(id);
  }

  public List<AllowedValueResponseDto> getAllowedValueDtos(String resourceClazz, String fieldName) {
    return allowedValueRepository
        .findByIdResourceClazzAndIdFieldName(resourceClazz, fieldName)
        .stream()
        .map(allowedValuesMapper::toDto)
        .toList();
  }
}
