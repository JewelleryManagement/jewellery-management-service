package jewellery.inventory.model.resource;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jewellery.inventory.model.ResourceInOrganization;
import jewellery.inventory.model.ResourceInUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
public class Resource {

  @Id @GeneratedValue private UUID id;

  private String clazz;
  private String quantityType;
  private BigDecimal pricePerQuantity;
  private String note;

  @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
  private List<ResourceInUser> userAffiliations;

  @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
  private List<ResourceInOrganization> organizationAffiliations;

  @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
  private List<ResourceInProduct> productAffiliations;
}
