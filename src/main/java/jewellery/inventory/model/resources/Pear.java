package jewellery.inventory.model.resources;

import javax.persistence.Entity;

@Entity
public class Pear extends Resource {
    private String type;
    private double size;
    private String quality;
    private String shape;

    // ------ Getters and setters ------ //


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }
}
