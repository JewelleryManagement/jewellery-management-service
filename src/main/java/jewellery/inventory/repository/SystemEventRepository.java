package jewellery.inventory.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.response.SystemEventLiteResponseDto;
import jewellery.inventory.dto.response.SystemEventResponseDto;
import jewellery.inventory.model.SystemEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SystemEventRepository extends JpaRepository<SystemEvent, UUID> {
  @Query(
"""
  select new jewellery.inventory.dto.response.SystemEventLiteResponseDto(
      e.id,
      e.timestamp,
      e.type,
      e.executor
  )
  from SystemEvent e
  order by e.timestamp desc
""")
  List<SystemEventLiteResponseDto> findAllWithoutRelatedIds();

  @Query(
"""
  select new jewellery.inventory.dto.response.SystemEventResponseDto(
      e.id,
      e.timestamp,
      e.type,
      e.executor,
      e.payload
  )
  from SystemEvent e
  where e.id = :eventId
""")
  Optional<SystemEventResponseDto> findByIdWithoutRelatedIds(@Param("eventId") UUID eventId);

  @Query(
"""
  select new jewellery.inventory.dto.response.SystemEventLiteResponseDto(
    e.id,
    e.timestamp,
    e.type,
    e.executor
  )
  from SystemEvent e
  join e.relatedIds r
  where r = :idValue
  order by e.timestamp desc
""")
  List<SystemEventLiteResponseDto> findByRelatedId(@Param("idValue") UUID idValue);

  @Query(
"""
  select distinct new jewellery.inventory.dto.response.SystemEventLiteResponseDto(
    e.id,
    e.timestamp,
    e.type,
    e.executor
  )
  from SystemEvent e
  join e.relatedIds r
  where r in :organizationIds
  order by e.timestamp desc
""")
  List<SystemEventLiteResponseDto> findByRelatedOrganizationIds(
      @Param("organizationIds") Collection<UUID> organizationIds);

  @Query(
      """
            select new jewellery.inventory.dto.response.SystemEventLiteResponseDto(
              e.id,
              e.timestamp,
              e.type,
              e.executor
            )
            from SystemEvent e
            join e.relatedIds requestedRelatedId
            where requestedRelatedId = :idValue
              and exists (
                select 1
                from SystemEvent e2
                join e2.relatedIds readableOrganizationId
                where e2.id = e.id
                  and readableOrganizationId in :organizationIds
              )
            order by e.timestamp desc
          """)
  List<SystemEventLiteResponseDto> findByRelatedIdAndRelatedOrganizationIds(
      @Param("idValue") UUID idValue, @Param("organizationIds") List<UUID> organizationIds);

  @Query(
      """
            select new jewellery.inventory.dto.response.SystemEventResponseDto(
              e.id,
              e.timestamp,
              e.type,
              e.executor,
              e.payload
            )
            from SystemEvent e
            where e.id = :id
              and exists (
                select 1
                from SystemEvent e2
                join e2.relatedIds readableOrganizationId
                where e2.id = e.id
                  and readableOrganizationId in :organizationIds
              )
          """)
  Optional<SystemEventResponseDto> findByIdAndRelatedOrganizationIds(
      @Param("id") UUID id, @Param("organizationIds") List<UUID> organizationIds);
}
