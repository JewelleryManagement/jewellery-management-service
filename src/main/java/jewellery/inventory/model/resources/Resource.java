package jewellery.inventory.model.resources;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
public class Resource {

    @Id
    @GeneratedValue
    private UUID id;

    private String clazz;
    private String quantityType;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    private List<ResourceInUser> userAffiliations;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    private List<ResourceInProduct> productAffiliations;
}
