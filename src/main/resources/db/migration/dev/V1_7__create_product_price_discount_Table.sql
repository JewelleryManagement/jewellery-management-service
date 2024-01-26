ALTER TABLE Product
ADD COLUMN additional_price DECIMAL(10, 2);

ALTER TABLE Product
DROP COLUMN sale_price CASCADE,
DROP COLUMN discount CASCADE;

CREATE TABLE product_price_discount (
    id UUID PRIMARY KEY,
    product_id UUID,
    sale_id UUID,
    sale_price DECIMAL(10, 2),
    discount DECIMAL(10, 2),
    FOREIGN KEY (product_id) REFERENCES Product(id),
    FOREIGN KEY (sale_id) REFERENCES Sale(id)
);
