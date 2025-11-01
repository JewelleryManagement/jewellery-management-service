package jewellery.inventory.repository;

import jewellery.inventory.model.resource.AllowedValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllowedValueRepository extends JpaRepository<AllowedValue, AllowedValue.AllowedValueId> {
    List<AllowedValue> findByIdResourceClazzAndIdFieldName(String resourceClazz, String fieldName);
} 