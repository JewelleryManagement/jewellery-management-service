package jewellery.inventory.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;

import lombok.*;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "organizations")
public class Organization {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private String name;

  @Column private String address;
  @Column private String note;

  @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
  private List<ResourceInOrganization> resourceInOrganization;

  @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
  private List<Product> productsOwned;

  @OneToMany(mappedBy = "organization", orphanRemoval = true, cascade = CascadeType.PERSIST)
  private List<UserInOrganization> usersInOrganization;

  @OneToMany(mappedBy = "organizationSeller", cascade = CascadeType.ALL)
  private List<Sale> sales;
}
