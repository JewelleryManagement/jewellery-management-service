ALTER TABLE public.product
ADD COLUMN additional_price DECIMAL(10, 2),
DROP COLUMN IF EXISTS part_of_sale_id CASCADE,
DROP COLUMN IF EXISTS sale_price CASCADE,
DROP COLUMN IF EXISTS discount CASCADE;

CREATE TABLE public.product_price_discount (
    id UUID PRIMARY KEY,
    product_id UUID,
    sale_id UUID,
    sale_price DECIMAL(10, 2),
    discount DECIMAL(10, 2),
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (sale_id) REFERENCES sale(id)
);

ALTER TABLE public.product
ADD COLUMN IF NOT EXISTS part_of_sale_id UUID,
ADD CONSTRAINT fk_product_price_discount_id FOREIGN KEY (part_of_sale_id) REFERENCES public.product_price_discount(id);

DELETE FROM Sale;