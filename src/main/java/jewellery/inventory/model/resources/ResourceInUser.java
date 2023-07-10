package jewellery.inventory.model.resources;

import jewellery.inventory.model.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
@Getter
@Setter
public class ResourceInUser {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private User owner;

    @ManyToOne
    private Resource resource;

    private double quantity;
}
