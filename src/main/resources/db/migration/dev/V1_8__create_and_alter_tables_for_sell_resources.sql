CREATE TABLE IF NOT EXISTS public.purchased_resources (
    id UUID PRIMARY KEY,
    resource_id UUID,
    owner_id UUID,
    quantity NUMERIC,
    sale_price NUMERIC,
    discount NUMERIC,
    part_of_sale_id UUID NULL
);

ALTER TABLE purchased_resources
    ADD CONSTRAINT fk_resource_id
    FOREIGN KEY (resource_id)
    REFERENCES public.resource(id);

ALTER TABLE purchased_resources
    ADD CONSTRAINT fk_part_of_sale_id
    FOREIGN KEY (part_of_sale_id)
    REFERENCES public.sale(id);

ALTER TABLE purchased_resources
    ADD CONSTRAINT fk_owner_id
    FOREIGN KEY (owner_id)
    REFERENCES public.users(id);

UPDATE public.resource
SET price_per_quantity=20;

UPDATE public.resource_in_user
SET quantity=100;


