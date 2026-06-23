package jewellery.inventory.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.RoleMembership;
import jewellery.inventory.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RoleMembershipRepository extends JpaRepository<RoleMembership, UUID> {

  @Query(
      """
      select count(m) > 0
      from RoleMembership m
      join m.role r
      join r.permissions perm
      where m.user.id = :userId
        and m.organization.id = :organizationId
        and perm = :permission
      """)
  boolean hasPermissionInOrganization(UUID userId, UUID organizationId, Permission permission);

  @Query(
      """
      select count(m) > 0
      from RoleMembership m
      join m.role r
      join r.permissions perm
      join Product p on p.organization.id = m.organization.id
      where p.id = :productId
        and m.user.id = :userId
        and perm = :permission
      """)
  boolean hasAccessToProduct(UUID productId, UUID userId, Permission permission);

  @Query(
      """
      select count(m) > 0
      from RoleMembership m
      join m.role r
      join r.permissions perm
      join Sale s on s.organizationSeller.id = m.organization.id
      where s.id = :saleId
        and m.user.id = :userId
        and perm = :permission
      """)
  boolean hasAccessToSale(UUID saleId, UUID userId, Permission permission);

  @Query(
      """
          select count(m) > 0
          from RoleMembership m
          join m.role r
          join r.permissions perm
          where m.user.id = :userId
            and m.organization is null
            and r.roleType = :roleType
            and perm = :permission
          """)
  boolean hasSystemPermission(UUID userId, RoleType roleType, Permission permission);

  boolean existsByRoleId(UUID roleId);

  @Query(
      """
        select distinct p
        from RoleMembership rm
        join rm.role r
        join r.permissions p
        where rm.user.id = :userId
          and rm.organization.id = :organizationId
    """)
  Set<Permission> findPermissionsByUserIdAndOrganizationId(
      @Param("userId") UUID userId, @Param("organizationId") UUID organizationId);

  @Query(
"""
    select distinct rm
    from RoleMembership rm
    join fetch rm.role r
    left join fetch r.permissions
    where rm.organization.id = :organizationId
      and rm.user.id in :userIds
""")
  List<RoleMembership> findAllByOrganizationIdAndUserIds(
      @Param("organizationId") UUID organizationId, @Param("userIds") List<UUID> userIds);

  @Modifying
  @Query(
"""
  DELETE FROM RoleMembership rm
  WHERE rm.user.id = :userId
    AND rm.organization.id = :organizationId
""")
  void deleteAllByUserAndOrganization(
      @Param("userId") UUID userId, @Param("organizationId") UUID organizationId);

  @Modifying
  @Transactional
  @Query(
      value =
          """
        insert into role_memberships (id, user_id, organization_id, role_id)
        select gen_random_uuid(), :userId, :organizationId, role_id
        from unnest(cast(:roleIds as uuid[])) as role_id
        """,
      nativeQuery = true)
  void insertAll(
      @Param("userId") UUID userId,
      @Param("organizationId") UUID organizationId,
      @Param("roleIds") UUID[] roleIds);

  @Query(
      """
            select distinct p
            from RoleMembership rm
            join rm.role r
            join r.permissions p
            where rm.user.id = :userId
              and rm.organization is null
              and r.roleType = :roleType
        """)
  Set<Permission> findSystemPermissionsByUserId(
      @Param("userId") UUID userId, @Param("roleType") RoleType roleType);

  @Query(
      """
        select distinct m.organization.id
        from RoleMembership m
        join m.role r
        join r.permissions perm
        where m.user.id = :userId
          and m.organization is not null
          and r.roleType = :roleType
          and perm = :permission
    """)
  List<UUID> findOrganizationIdsByUserIdAndPermission(
      @Param("userId") UUID userId,
      @Param("roleType") RoleType roleType,
      @Param("permission") Permission permission);

  @Modifying
  @Query(
      """
    delete from RoleMembership rm
    where rm.user.id = :userId
      and rm.organization is null
    """)
  void deleteAllSystemRolesByUserId(UUID userId);

  @Query(
      """
    select distinct rm
    from RoleMembership rm
    join fetch rm.role r
    left join fetch r.permissions
    where rm.user.id = :userId
      and rm.organization is null
    """)
  List<RoleMembership> findAllSystemRolesByUserId(@Param("userId") UUID userId);
}
