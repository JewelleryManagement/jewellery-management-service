create table if not exists system_event_related (
  event_id   uuid not null,
  related_id uuid not null,
  constraint fk_system_event_related_event
    foreign key (event_id) references system_event(id) on delete cascade,
  constraint pk_system_event_related
    primary key (event_id, related_id)
);

INSERT INTO system_event_related (event_id, related_id)
WITH selected AS (
  SELECT
    se.id AS event_id,
    jsonb_build_object('payload', se.payload, 'executor', se.executor) AS j
  FROM system_event se
),
strings AS (
  SELECT
    s.event_id,
    v #>> '{}' AS txt
  FROM selected s
  CROSS JOIN LATERAL jsonb_path_query(
    s.j,
    '$.** ? (@.type() == "string")'
  ) AS v
),
uuids AS (
  SELECT
    event_id,
    (m[1])::uuid AS related_id
  FROM strings
  CROSS JOIN LATERAL regexp_matches(
    txt,
    '([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})',
    'gi'
  ) AS m
)
SELECT DISTINCT event_id, related_id
FROM uuids
ON CONFLICT DO NOTHING;