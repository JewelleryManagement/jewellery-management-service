package jewellery.inventory.model.resources;

import javax.persistence.Entity;

@Entity
public class LinkingPart extends Resource {
    private String description;

    // ------ Getters and setters ------ //


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
