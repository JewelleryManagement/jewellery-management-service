package jewellery.inventory.model.resources;

import javax.persistence.Entity;

@Entity
public class Gemstone extends Resources {
    private String color;
    private double carat;
    private String cut;
    private String clarity;
    private double dimensionX;
    private double dimensionY;
    private double dimensionZ;
    private String shape;

}
