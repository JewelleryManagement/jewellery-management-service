package jewellery.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "name")
    private String name;

    @ElementCollection
    private List<String> authors;

    @JsonIgnore
    @ManyToOne
    private User owner;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ResourceInProduct> resourcesContent;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "content_id")
    private Product content;
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    private List<Product> productsContent;

    private String description;
    private double salePrice;
    private boolean isSold;
}
