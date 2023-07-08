package jewellery.inventory.model.resources;

import jewellery.inventory.model.User;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
public class ResourceInUser {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private User owner;

    @ManyToOne
    private Resource resource;

    private double quanity;

    // ------ Getters and setters ------ //


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public double getQuanity() {
        return quanity;
    }

    public void setQuanity(double quanity) {
        this.quanity = quanity;
    }
}
