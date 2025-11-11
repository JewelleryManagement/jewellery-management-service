-- Create allowed_value table
CREATE TABLE allowed_value (
    resource_clazz VARCHAR(255) NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    PRIMARY KEY (resource_clazz, field_name, value)
);