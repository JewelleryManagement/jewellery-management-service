CREATE TABLE organization_roles (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE organization_role_permissions (
    role_id UUID NOT NULL,
    permission VARCHAR(100) NOT NULL,

    CONSTRAINT fk_organization_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES organization_roles (id)
        ON DELETE CASCADE,

    CONSTRAINT uk_organization_role_permissions_role_permission
        UNIQUE (role_id, permission)
);

CREATE TABLE organization_memberships (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    role_id UUID NOT NULL,

    CONSTRAINT fk_organization_memberships_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_organization_memberships_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_organization_memberships_role
        FOREIGN KEY (role_id)
        REFERENCES organization_roles (id)
        ON DELETE CASCADE,

    CONSTRAINT uk_org_membership_user_org_role
        UNIQUE (user_id, organization_id, role_id)
);

INSERT INTO organization_roles (id, name)
VALUES ('8f279d03-10d7-48bb-b985-dc51bcc52fdc', 'ORGANIZATION_ADMIN');

INSERT INTO organization_role_permissions (role_id, permission)
SELECT r.id, p.permission
FROM organization_roles r,
unnest(ARRAY[
    'ORGANIZATION_READ',
    'ORGANIZATION_USER_ADD',
    'ORGANIZATION_USER_DELETE',
    'ORGANIZATION_DELETE',
    'ORGANIZATION_PERMISSION_UPDATE',
    'ORGANIZATION_USER_READ',
    'ORGANIZATION_RESOURCE_ADD',
    'ORGANIZATION_RESOURCE_DELETE',
    'ORGANIZATION_RESOURCE_READ',
    'ORGANIZATION_RESOURCE_TRANSFER',
    'ORGANIZATION_PRODUCT_CREATE',
    'ORGANIZATION_PRODUCT_UPDATE',
    'ORGANIZATION_PRODUCT_DELETE',
    'ORGANIZATION_PRODUCT_TRANSFER',
    'ORGANIZATION_PRODUCT_READ',
    'ORGANIZATION_SALE_CREATE',
    'ORGANIZATION_SALE_PRODUCT_RETURN',
    'ORGANIZATION_SALE_READ',
    'ORGANIZATION_SALE_RESOURCE_RETURN',
    'ORGANIZATION_ROLE_ASSIGN',
    'ORGANIZATION_USER_ROLES_READ'
]) AS p(permission)
WHERE r.name = 'ORGANIZATION_ADMIN'
ON CONFLICT (role_id, permission) DO NOTHING;

INSERT INTO organization_memberships (id, user_id, organization_id, role_id)
SELECT
    gen_random_uuid(),
    src.user_id,
    src.organization_id,
    '8f279d03-10d7-48bb-b985-dc51bcc52fdc'
FROM (
    SELECT DISTINCT user_id, organization_id
    FROM user_in_organization
) src
WHERE NOT EXISTS (
    SELECT 1
    FROM organization_memberships om
    WHERE om.user_id = src.user_id
      AND om.organization_id = src.organization_id
      AND om.role_id = '8f279d03-10d7-48bb-b985-dc51bcc52fdc'
);
