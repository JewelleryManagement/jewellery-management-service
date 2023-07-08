package jewellery.inventory.model.resources;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class Resource {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String color;
    private String quantityType;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    private List<ResourceInUser> userAffiliations;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    private List<ResourceInProduct> productAffiliations;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getQuantityType() {
        return quantityType;
    }

    public void setQuantityType(String quantityType) {
        this.quantityType = quantityType;
    }

    public List<ResourceInUser> getUserAffiliations() {
        return userAffiliations;
    }

    public void setUserAffiliations(List<ResourceInUser> userAffiliations) {
        this.userAffiliations = userAffiliations;
    }

    public List<ResourceInProduct> getProductAffiliations() {
        return productAffiliations;
    }

    public void setProductAffiliations(List<ResourceInProduct> productAffiliations) {
        this.productAffiliations = productAffiliations;
    }
}
