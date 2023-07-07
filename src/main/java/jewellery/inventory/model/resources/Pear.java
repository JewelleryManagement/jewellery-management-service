package jewellery.inventory.model.resources;

import javax.persistence.Entity;

@Entity
public class Pear extends Resources {
    private String type;
    private double size;
    private String quality;
    private String shape;
}
