ALTER TABLE public.product
ADD COLUMN additional_price DECIMAL(10, 2);

ALTER TABLE public.product
DROP COLUMN sale_price CASCADE,
DROP COLUMN discount CASCADE;

CREATE TABLE public.product_price_discount (
    id UUID PRIMARY KEY,
    product_id UUID,
    sale_id UUID,
    sale_price DECIMAL(10, 2),
    discount DECIMAL(10, 2),
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (sale_id) REFERENCES sale(id)
);