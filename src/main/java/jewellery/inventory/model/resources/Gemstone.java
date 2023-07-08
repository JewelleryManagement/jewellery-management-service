package jewellery.inventory.model.resources;

import javax.persistence.Entity;

@Entity
public class Gemstone extends Resource {
    private String color;
    private double carat;
    private String cut;
    private String clarity;
    private double dimensionX;
    private double dimensionY;
    private double dimensionZ;
    private String shape;


    // ------ Getters and setters ------ //


    @Override
    public String getColor() {
        return color;
    }

    @Override
    public void setColor(String color) {
        this.color = color;
    }

    public double getCarat() {
        return carat;
    }

    public void setCarat(double carat) {
        this.carat = carat;
    }

    public String getCut() {
        return cut;
    }

    public void setCut(String cut) {
        this.cut = cut;
    }

    public String getClarity() {
        return clarity;
    }

    public void setClarity(String clarity) {
        this.clarity = clarity;
    }

    public double getDimensionX() {
        return dimensionX;
    }

    public void setDimensionX(double dimensionX) {
        this.dimensionX = dimensionX;
    }

    public double getDimensionY() {
        return dimensionY;
    }

    public void setDimensionY(double dimensionY) {
        this.dimensionY = dimensionY;
    }

    public double getDimensionZ() {
        return dimensionZ;
    }

    public void setDimensionZ(double dimensionZ) {
        this.dimensionZ = dimensionZ;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }
}
