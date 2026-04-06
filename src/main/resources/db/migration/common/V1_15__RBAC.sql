CREATE TABLE scoped_roles (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission VARCHAR(100) NOT NULL,

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES scoped_roles (id)
        ON DELETE CASCADE,

    CONSTRAINT uk_role_permissions_role_permission
        UNIQUE (role_id, permission)
);

CREATE TABLE role_memberships (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    role_id UUID NOT NULL,

    CONSTRAINT fk_role_memberships_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_memberships_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_memberships_role
        FOREIGN KEY (role_id)
        REFERENCES scoped_roles (id)
        ON DELETE CASCADE,

    CONSTRAINT uk_org_membership_user_org_role
        UNIQUE (user_id, organization_id, role_id)
);

INSERT INTO scoped_roles (id, name)
VALUES ('8f279d03-10d7-48bb-b985-dc51bcc52fdc', 'ORGANIZATION_ADMIN');

INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission
FROM scoped_roles r,
unnest(ARRAY[
    'organization:read',
    'organization:user:add',
    'organization:user:delete',
    'organization:delete',
    'organization:permission:update',
    'organization:user:read',
    'organization:resource:add',
    'organization:resource:delete',
    'organization:resource:read',
    'organization:resource:transfer',
    'organization:product:create',
    'organization:product:update',
    'organization:product:delete',
    'organization:product:transfer',
    'organization:product:read',
    'organization:sale:create',
    'organization:sale:product:return',
    'organization:sale:read',
    'organization:sale:resource:return',
    'organization:role:assign',
    'organization:user:roles:read'
]) AS p(permission)
WHERE r.name = 'ORGANIZATION_ADMIN'
ON CONFLICT (role_id, permission) DO NOTHING;

INSERT INTO role_memberships (id, user_id, organization_id, role_id)
SELECT
    gen_random_uuid(),
    src.user_id,
    src.organization_id,
    '8f279d03-10d7-48bb-b985-dc51bcc52fdc'
FROM (
    SELECT DISTINCT uio.user_id, uio.organization_id
    FROM user_in_organization uio
    JOIN users u ON u.id = uio.user_id
    WHERE u.role = 'ADMIN'
) src
WHERE NOT EXISTS (
    SELECT 1
    FROM role_memberships rm
    WHERE rm.user_id = src.user_id
      AND rm.organization_id = src.organization_id
      AND rm.role_id = '8f279d03-10d7-48bb-b985-dc51bcc52fdc'
);
