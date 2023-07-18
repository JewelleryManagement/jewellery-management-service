package jewellery.inventory.model.resource;

import jewellery.inventory.model.Product;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.UUID;

@Entity
@Getter
@Setter
public class ResourceInProduct {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Resource resource;

    private double quantity;

    @ManyToOne
    private Product product;
}
