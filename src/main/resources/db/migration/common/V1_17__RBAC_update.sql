ALTER TABLE role_memberships
ALTER COLUMN organization_id DROP NOT NULL;

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
    'system:organization:create'
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