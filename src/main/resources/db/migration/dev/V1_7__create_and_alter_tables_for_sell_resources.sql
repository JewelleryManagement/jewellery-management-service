DROP TABLE IF EXISTS purchased_resources;

CREATE TABLE IF NOT EXISTS public.purchased_resources (
    id UUID PRIMARY KEY,
    resource_id UUID,
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

