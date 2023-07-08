package jewellery.inventory.model.resources;

import jewellery.inventory.model.Product;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
public class ResourceInProduct {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Resource resource;

    private double quantity;

    @ManyToOne
    private Product product;

    // ------ Getters and setters ------ //


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
