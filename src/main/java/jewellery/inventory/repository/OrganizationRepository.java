package jewellery.inventory.repository;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
  @Query(
"""
    select distinct o
    from OrganizationMembership m
    join m.organization o
    join m.roles r
    join r.permissions p
    where m.user.id = :userId
      and p = :permission
""")
  List<Organization> findOrganizationsByUserIdAndPermission(UUID userId, Permission permission);
}
