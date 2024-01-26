ALTER TABLE Product
ADD COLUMN additional_price DECIMAL(10, 2);

ALTER TABLE Product
DROP COLUMN sale_price CASCADE,
DROP COLUMN discount CASCADE;

UPDATE resource
SET price_per_quantity = 1.0;

CREATE TABLE product_price_discount (
    id UUID PRIMARY KEY,
    product_id UUID,
    sale_id UUID,
    sale_price DECIMAL(10, 2),
    discount DECIMAL(10, 2),
    FOREIGN KEY (product_id) REFERENCES Product(id),
    FOREIGN KEY (sale_id) REFERENCES Sale(id)
);

INSERT INTO product_price_discount (id, product_id, sale_id, sale_price, discount)
VALUES ('434ebade-3dad-4d13-888e-765e56635f05','e0f72130-f83d-4ca7-845b-aa82f2bb9a22', '38839f01-ed22-4a15-86eb-125d7cceafd9', 5.0, 0.0);
