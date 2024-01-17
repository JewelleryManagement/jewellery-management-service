CREATE TABLE public.purchased_resources (
    id UUID PRIMARY KEY,
    resource_id UUID,
    quantity NUMERIC,
    sale_price NUMERIC,
    discount NUMERIC,
    part_of_sale VARCHAR(255),
    FOREIGN KEY (resource_id) REFERENCES resources(id),
    FOREIGN KEY (part_of_sale_id) REFERENCES public.sale(id)
);

