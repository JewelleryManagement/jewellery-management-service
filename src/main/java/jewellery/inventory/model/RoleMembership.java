package jewellery.inventory.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "role_memberships",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"user_id", "organization_id", "role_id"})
    })
public class RoleMembership {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id")
  private Organization organization;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "role_id", nullable = false)
  private ScopedRole role;

  @PrePersist
  @PreUpdate
  private void validateScope() {
    if (role == null || role.getRoleType() == null) {
      return;
    }

    if (role.getRoleType() == RoleType.ORGANIZATION && organization == null) {
      throw new IllegalStateException("Organization role membership requires organization");
    }

    if (role.getRoleType() == RoleType.SYSTEM && organization != null) {
      throw new IllegalStateException("System role membership must not have organization");
    }
  }
}
