package jewellery.inventory.service;

import jewellery.inventory.model.resource.AllowedValue;
import jewellery.inventory.repository.AllowedValueRepository;
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
        allowedValueRepository.deleteById(id);
    }

    public List<AllowedValue> getAllowedValues(String resourceClazz, String fieldName) {
        return allowedValueRepository.findByIdResourceClazzAndIdFieldName(resourceClazz, fieldName);
    }
} 