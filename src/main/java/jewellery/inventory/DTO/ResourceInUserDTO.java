package jewellery.inventory.DTO;

import jewellery.inventory.model.User;
import jewellery.inventory.model.resources.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ManyToOne;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceInUserDTO {
    private UUID id;

    @ManyToOne
    private User owner;

    @ManyToOne
    private Resource resource;

    private double quantity;
}
