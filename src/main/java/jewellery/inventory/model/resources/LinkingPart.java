package jewellery.inventory.model.resources;


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
public class LinkingPart extends Resource {
    private String description;

}
