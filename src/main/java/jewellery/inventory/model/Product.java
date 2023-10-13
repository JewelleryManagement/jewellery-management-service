package jewellery.inventory.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import jewellery.inventory.model.resource.ResourceInProduct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class Product implements Serializable {
    @Id
    @GeneratedValue
    private UUID id;

    @ElementCollection
    private List<String> authors;

    @ManyToOne
    private User owner;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ResourceInProduct> resourcesContent;

    @ManyToOne
    @JoinColumn(name = "content_of")
    private Product contentOf;
    @OneToMany(mappedBy = "contentOf", cascade = CascadeType.ALL)
    private List<Product> productsContent;

    private String description;
    private double salePrice;
    private boolean isSold;
}
