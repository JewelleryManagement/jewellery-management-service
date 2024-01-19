CREATE TABLE product_price_discount (
    id UUID PRIMARY KEY,
    product_id UUID,
    sale_price DECIMAL,
    discount DECIMAL,
    FOREIGN KEY (product_id) REFERENCES Product(id)
);
