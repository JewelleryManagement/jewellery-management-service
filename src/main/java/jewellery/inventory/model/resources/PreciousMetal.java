package jewellery.inventory.model.resources;

import javax.persistence.Entity;

@Entity
public class PreciousMetal extends Resource {
    private String type;
    private int purity;
    private String plating;

    // ------ Getters and setters ------ //


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPurity() {
        return purity;
    }

    public void setPurity(int purity) {
        this.purity = purity;
    }

    public String getPlating() {
        return plating;
    }

    public void setPlating(String plating) {
        this.plating = plating;
    }
}
