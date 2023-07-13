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
public class Gemstone extends Resource {
    private String color;
    private double carat;
    private String cut;
    private String clarity;
    private double dimensionX;
    private double dimensionY;
    private double dimensionZ;
    private String shape;
}
