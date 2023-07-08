package jewellery.inventory.model;

import jewellery.inventory.model.resources.ResourceInProduct;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @ElementCollection
    private List<String> authors;

    @ManyToOne
    private User owner;

    @Lob
    private byte[] picture;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ResourceInProduct> resourcesContent;

    @ManyToMany
    private List<Product> productsContent;

    private String description;
    private double salePrice;
    private boolean isSold;

    // ------ Getters and setters ------ //

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public List<ResourceInProduct> getResourcesContent() {
        return resourcesContent;
    }

    public void setResourcesContent(List<ResourceInProduct> resourcesContent) {
        this.resourcesContent = resourcesContent;
    }

    public List<Product> getProductsContent() {
        return productsContent;
    }

    public void setProductsContent(List<Product> productsContent) {
        this.productsContent = productsContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public boolean isSold() {
        return isSold;
    }

    public void setSold(boolean sold) {
        isSold = sold;
    }
}
