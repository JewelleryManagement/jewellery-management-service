package jewellery.inventory.repository;

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
}
