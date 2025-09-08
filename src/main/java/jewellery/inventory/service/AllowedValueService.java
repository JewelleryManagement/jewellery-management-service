package jewellery.inventory.service;

import jewellery.inventory.model.resource.AllowedValue;
import jewellery.inventory.repository.AllowedValueRepository;
import jewellery.inventory.dto.response.resource.AllowedValueResponseDto;
import jewellery.inventory.exception.not_found.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AllowedValueService {
    private final AllowedValueRepository allowedValueRepository;

    public AllowedValueService(AllowedValueRepository allowedValueRepository) {
        this.allowedValueRepository = allowedValueRepository;
    }

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
        return allowedValueRepository.findByIdResourceClazzAndIdFieldName(resourceClazz, fieldName)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AllowedValueResponseDto toDto(AllowedValue allowedValue) {
        return new AllowedValueResponseDto(
                allowedValue.getId().getResourceClazz(),
                allowedValue.getId().getFieldName(),
                allowedValue.getId().getValue()
        );
    }
} 