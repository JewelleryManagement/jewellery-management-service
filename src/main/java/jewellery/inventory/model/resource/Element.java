package jewellery.inventory.model.resource;


import lombok.*;

import jakarta.persistence.Entity;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class Element extends Resource {
    private String description;

}
