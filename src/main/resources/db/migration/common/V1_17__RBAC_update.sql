ALTER TABLE role_memberships
ALTER COLUMN organization_id DROP NOT NULL;

create or replace function validate_role_membership_scope()
returns trigger
language plpgsql
as $$
declare
    role_type_value varchar;
begin
    select role_type
    into role_type_value
    from scoped_roles
    where id = new.role_id;

    if role_type_value is null then
        raise exception 'Role % does not exist', new.role_id;
    end if;

    if role_type_value = 'SYSTEM'
       and new.organization_id is not null then
        raise exception using
            errcode = '23514',
            message = 'System role membership must not have an organization';
    end if;

    if role_type_value = 'ORGANIZATION'
       and new.organization_id is null then
        raise exception using
            errcode = '23514',
            message = 'Organization role membership requires an organization';
    end if;

    return new;
end;
$$;

create trigger trg_validate_role_membership_scope
before insert or update of role_id, organization_id
on role_memberships
for each row
execute function validate_role_membership_scope();

create unique index uq_role_membership_organization
on role_memberships (user_id, organization_id, role_id)
where organization_id is not null;

create unique index uq_role_membership_system
on role_memberships (user_id, role_id)
where organization_id is null;

INSERT INTO scoped_roles (
    id,
    name,
    role_type
) VALUES (
    '7f2d3b9a-6c41-4e58-9fd6-2f8f789f12a1',
    'SYSTEM_ADMIN',
    'SYSTEM'
);

INSERT INTO role_permissions (role_id, permission)
SELECT
  '7f2d3b9a-6c41-4e58-9fd6-2f8f789f12a1'::uuid,
  permission
FROM unnest(ARRAY[
    'system:user:read',
    'system:user:create',
    'system:user:update',
    'system:user:delete',
    'system:resource:read',
    'system:resource:create',
    'system:resource:update',
    'system:resource:delete',
    'system:resource:import',
    'system:role:create',
    'system:role:delete',
    'system:role:read',
    'system:event:read',
    'system:organization:create',
    'system:role:assign'
]) AS permission;

INSERT INTO role_memberships (
    id,
    user_id,
    organization_id,
    role_id
) VALUES (
    'c9a13e4b-2d79-45c1-8b9f-47e42c8a92d3',
    '88596531-7f0f-407d-b502-31833b8c8e8d',
    null,
    '7f2d3b9a-6c41-4e58-9fd6-2f8f789f12a1'
);