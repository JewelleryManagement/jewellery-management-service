package jewellery.inventory.model.resources;


import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;

@Entity
@Getter
@Setter
public class LinkingPart extends Resource {
    private String description;

}
