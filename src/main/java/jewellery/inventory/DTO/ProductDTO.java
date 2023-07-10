package jewellery.inventory.DTO;

import jewellery.inventory.model.Product;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resources.ResourceInProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
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
}
