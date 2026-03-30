create table if not exists organization_roles (
    id uuid primary key,
    name varchar(255) not null
);

create table if not exists organization_memberships (
    id uuid primary key,
    user_id uuid not null,
    organization_id uuid not null,
    constraint uk_organization_memberships_user_org
        unique (user_id, organization_id),
    constraint fk_organization_memberships_user
        foreign key (user_id) references users(id) on delete cascade,
    constraint fk_organization_memberships_organization
        foreign key (organization_id) references organizations(id) on delete cascade
);

create table if not exists organization_membership_roles (
    membership_id uuid not null,
    role_id uuid not null,
    primary key (membership_id, role_id),
    constraint fk_membership_roles_membership
        foreign key (membership_id) references organization_memberships(id) on delete cascade,
    constraint fk_membership_roles_role
        foreign key (role_id) references organization_roles(id) on delete cascade
);

create table if not exists organization_role_permissions (
    role_id uuid not null,
    permission varchar(100) not null,
    primary key (role_id, permission),
    constraint fk_role_permissions_role
        foreign key (role_id) references organization_roles(id) on delete cascade
);
