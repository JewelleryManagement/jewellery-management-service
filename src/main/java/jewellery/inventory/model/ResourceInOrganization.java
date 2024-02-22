package jewellery.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.model.resource.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@EqualsAndHashCode
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceInOrganization {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne
  private Organization organization;

  @ManyToOne private Resource resource;

  private BigDecimal quantity;

  @Transient
  private BigDecimal dealPrice;
}
