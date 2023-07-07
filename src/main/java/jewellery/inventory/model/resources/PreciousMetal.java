package jewellery.inventory.model.resources;

import javax.persistence.Entity;

@Entity
public class PreciousMetal extends Resources {
    private String type;
    private int purity;
    private String plating;

}
