package jewellery.inventory.model;

import jewellery.inventory.model.resources.ResourceInUser;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL.ALL)
    private List<Product> productsOwned;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ResourceInUser> resourcesOwned;

    // ------ Getters and setters ------ //

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Product> getProductsOwned() {
        return productsOwned;
    }

    public void setProductsOwned(List<Product> productsOwned) {
        this.productsOwned = productsOwned;
    }

    public List<ResourceInUser> getResourcesOwned() {
        return resourcesOwned;
    }

    public void setResourcesOwned(List<ResourceInUser> resourcesOwned) {
        this.resourcesOwned = resourcesOwned;
    }
}
