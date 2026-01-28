create table if not exists system_event_related (
  event_id   uuid not null,
  related_id uuid not null,
  constraint fk_system_event_related_event
    foreign key (event_id) references system_event(id) on delete cascade,
  constraint pk_system_event_related
    primary key (event_id, related_id)
);