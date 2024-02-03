package jewellery.inventory.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.List;
import java.util.UUID;
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
public class UserInOrganization {

  @Id
  @GeneratedValue
  private UUID id;
  @ManyToOne
  private User user;
  @ManyToOne
  private Organization organization;
  @ElementCollection(targetClass = OrganizationPermission.class)
  @CollectionTable
  @Enumerated(EnumType.STRING)
  private List<OrganizationPermission> organizationPermission;

}
