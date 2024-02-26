package jewellery.inventory.model;

import jakarta.persistence.*;

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
@Table(name = "user_in_organization")
public class UserInOrganization {
  @Id @GeneratedValue private UUID id;
  @ManyToOne private User user;
  @ManyToOne
  @JoinColumn(name = "organization_id")
  private Organization organization;
  @Enumerated(EnumType.STRING)
  @Column(name = "organization_permission")
  private List<OrganizationPermission> organizationPermission;
}
