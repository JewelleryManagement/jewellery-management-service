package jewellery.inventory.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "organization_memberships",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "organization_id"})})
public class OrganizationMembership {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "organization_membership_roles",
      joinColumns = @JoinColumn(name = "membership_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<OrganizationRole> roles = new HashSet<>();
}
