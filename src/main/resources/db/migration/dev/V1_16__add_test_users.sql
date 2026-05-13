INSERT INTO users (
    id,
    first_name,
    last_name,
    email,
    password,
    role
) VALUES
(
    gen_random_uuid(),
    'UserWithoutPermissions',
    'UserWithoutPermissions',
    'withoutPermissions@gmail.com',
    '$2a$10$GBjllOzjHSeTnu4R5jbIUONnGomCq6yGHyMw6BTPNEnMPZ5rAYHsO',
    'ADMIN'
);

INSERT INTO users (
    id,
    first_name,
    last_name,
    email,
    password,
    role
) VALUES
(
    'a3f6c2b2-7b1e-4c5a-9f6d-123456789abc',
    'UserWithReadPermissions',
    'UserWithReadPermissions',
    'withReadPermissions@gmail.com',
    '$2a$10$GBjllOzjHSeTnu4R5jbIUONnGomCq6yGHyMw6BTPNEnMPZ5rAYHsO',
    'ADMIN'
);

INSERT INTO users (
    id,
    first_name,
    last_name,
    email,
    password,
    role
) VALUES
(
    '9f3f0c9e-7d8d-4a2a-9a7d-2e6a8c2f4d11',
    'UserWithSomePermissions',
    'UserWithSomePermissions',
    'withSomePermissions@gmail.com',
    '$2a$10$GBjllOzjHSeTnu4R5jbIUONnGomCq6yGHyMw6BTPNEnMPZ5rAYHsO',
    'ADMIN'
);

INSERT INTO user_in_organization (
    id,
    user_id,
    organization_id
) VALUES (
    gen_random_uuid(),
    'a3f6c2b2-7b1e-4c5a-9f6d-123456789abc',
    '640809ce-f04b-46c3-9a01-9cd33034d185'
);

INSERT INTO user_in_organization (
    id,
    user_id,
    organization_id
) VALUES (
    gen_random_uuid(),
    '9f3f0c9e-7d8d-4a2a-9a7d-2e6a8c2f4d11',
    '640809ce-f04b-46c3-9a01-9cd33034d185'
);

INSERT INTO scoped_roles (
    id,
    name,
    role_type
) VALUES (
    '8f3a2c6e-1d4b-4f7a-9c2e-6b8d1a2f9c01',
    'TEST_READ_ONLY',
    'ORGANIZATION'
);

INSERT INTO scoped_roles (
    id,
    name,
    role_type
) VALUES (
    '2c1b7a54-6f2d-48fd-b2d1-91f4b8d83a7c',
    'TEST_SOME_PERMISSIONS',
    'ORGANIZATION'
);

INSERT INTO role_permissions (
    role_id,
    permission
) VALUES
(
    '8f3a2c6e-1d4b-4f7a-9c2e-6b8d1a2f9c01',
    'organization:read'
);

INSERT INTO role_permissions (role_id, permission)
SELECT
  '2c1b7a54-6f2d-48fd-b2d1-91f4b8d83a7c'::uuid,
  permission
FROM unnest(ARRAY[
    'organization:read',
    'organization:user:read',
    'organization:product:read',
    'organization:resource:read',
    'organization:sale:read'
]) AS permission;

INSERT INTO role_memberships (
    id,
    user_id,
    organization_id,
    role_id
) VALUES (
    '5c9a1e8f-2d4b-4a71-9f6e-3b8d2c7a1e90',
    'a3f6c2b2-7b1e-4c5a-9f6d-123456789abc',
    '640809ce-f04b-46c3-9a01-9cd33034d185',
    '8f3a2c6e-1d4b-4f7a-9c2e-6b8d1a2f9c01'
);

INSERT INTO role_memberships (
    id,
    user_id,
    organization_id,
    role_id
) VALUES (
    'f6d9e1b2-3a74-4f59-8f42-5d7a3c0cbb90',
    '9f3f0c9e-7d8d-4a2a-9a7d-2e6a8c2f4d11',
    '640809ce-f04b-46c3-9a01-9cd33034d185',
    '2c1b7a54-6f2d-48fd-b2d1-91f4b8d83a7c'
);
