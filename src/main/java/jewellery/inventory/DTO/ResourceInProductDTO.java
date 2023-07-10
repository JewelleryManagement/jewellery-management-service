package jewellery.inventory.DTO;

import jewellery.inventory.model.Product;
import jewellery.inventory.model.resources.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ManyToOne;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceInProductDTO {
    private UUID id;

    @ManyToOne
    private Resource resource;

    private double quantity;

    @ManyToOne
    private Product product;
}
