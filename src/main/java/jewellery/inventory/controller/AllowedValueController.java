package jewellery.inventory.controller;

import jewellery.inventory.model.resource.AllowedValue;
import jewellery.inventory.service.AllowedValueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/allowed-values")
public class AllowedValueController {
    private final AllowedValueService allowedValueService;

    public AllowedValueController(AllowedValueService allowedValueService) {
        this.allowedValueService = allowedValueService;
    }

    @PostMapping
    public ResponseEntity<AllowedValue> addAllowedValue(@RequestBody AllowedValue allowedValue) {
        return ResponseEntity.ok(allowedValueService.addAllowedValue(allowedValue));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllowedValue(@RequestBody AllowedValue.AllowedValueId id) {
        allowedValueService.deleteAllowedValue(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AllowedValue>> getAllowedValues(@RequestParam String resourceClazz, @RequestParam String fieldName) {
        return ResponseEntity.ok(allowedValueService.getAllowedValues(resourceClazz, fieldName));
    }
} 