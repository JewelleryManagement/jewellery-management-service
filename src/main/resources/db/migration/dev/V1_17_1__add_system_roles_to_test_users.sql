INSERT INTO scoped_roles (
    id,
    name,
    role_type
) VALUES (
    '3f5b2c4e-8b6f-4e91-9c7a-1a0c2d8e4f73',
    'TEST_SYSTEM_READ_ONLY',
    'SYSTEM'
);

INSERT INTO scoped_roles (
    id,
    name,
    role_type
) VALUES (
    '9d374746-4f99-4dd4-892b-44ea25ec52d1',
    'TEST_SYSTEM_SOME_PERMISSIONS',
    'SYSTEM'
);

INSERT INTO role_permissions (role_id, permission)
SELECT
  '3f5b2c4e-8b6f-4e91-9c7a-1a0c2d8e4f73'::uuid,
  permission
FROM unnest(ARRAY[
    'system:user:read',
    'system:resource:read'
]) AS permission;

INSERT INTO role_permissions (role_id, permission)
SELECT
  '9d374746-4f99-4dd4-892b-44ea25ec52d1'::uuid,
  permission
FROM unnest(ARRAY[
    'system:user:read',
    'system:resource:read',
    'system:role:read'
]) AS permission;

INSERT INTO role_memberships (
    id,
    user_id,
    organization_id,
    role_id
) VALUES (
    'a9d4f621-3c8e-4b2f-91a7-6e5c0d3f8b12',
    'a3f6c2b2-7b1e-4c5a-9f6d-123456789abc',
    null,
    '3f5b2c4e-8b6f-4e91-9c7a-1a0c2d8e4f73'
);

INSERT INTO role_memberships (
    id,
    user_id,
    organization_id,
    role_id
) VALUES (
    'e9d09a50-0a9d-4e47-8522-b36ad2418963',
    '9f3f0c9e-7d8d-4a2a-9a7d-2e6a8c2f4d11',
    null,
    '9d374746-4f99-4dd4-892b-44ea25ec52d1'
);