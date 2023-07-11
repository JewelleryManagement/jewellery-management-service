package jewellery.inventory.model.resources;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
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
