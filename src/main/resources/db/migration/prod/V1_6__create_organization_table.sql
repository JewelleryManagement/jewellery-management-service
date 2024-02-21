CREATE TABLE public.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    note VARCHAR(255)
);

CREATE TABLE public.user_in_organization (
    id UUID PRIMARY KEY,
    user_id UUID,
    organization_id UUID,
    organization_permission VARCHAR(255) ARRAY,
    CONSTRAINT fk_user_user_in_org FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_org_user_in_org FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE resource_in_organization (
    id UUID PRIMARY KEY,
    organization_id UUID,
    resource_id UUID,
    quantity DECIMAL,
    deal_price DECIMAL,
    CONSTRAINT fk_org_resource_in_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_resource_resource_in_org FOREIGN KEY (resource_id) REFERENCES resource(id)
);

ALTER TABLE product ADD COLUMN organization_id UUID;
ALTER TABLE product ADD CONSTRAINT fk_org_owner FOREIGN KEY (organization_id) REFERENCES organizations(id);

ALTER TABLE sale ADD COLUMN organization_seller_id UUID;
ALTER TABLE sale ADD CONSTRAINT fk_org_seller FOREIGN KEY (organization_seller_id) REFERENCES organizations(id);

