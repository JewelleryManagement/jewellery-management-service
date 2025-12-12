ALTER TABLE public.resource
ADD COLUMN shape_specification VARCHAR(255) NULL,
ADD COLUMN color_hue VARCHAR(255) NULL,
ADD COLUMN polish VARCHAR(255) NULL,
ADD COLUMN symmetry VARCHAR(255) NULL,
ADD COLUMN fluorescence VARCHAR(255) NULL,
ADD COLUMN certificate VARCHAR(255) NULL,
ADD COLUMN treatment VARCHAR(255) NULL,
DROP COLUMN plating,
ALTER COLUMN size TYPE VARCHAR(255),
ALTER COLUMN carat TYPE NUMERIC(10,5);

ALTER TABLE allowed_value
ADD COLUMN sku VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE allowed_value
DROP CONSTRAINT allowed_value_pkey;

ALTER TABLE allowed_value
ADD CONSTRAINT allowed_value_pkey
PRIMARY KEY (resource_clazz, field_name, value, sku);

ALTER TABLE public.resource
ADD COLUMN sku VARCHAR(255) NOT NULL DEFAULT '';

INSERT INTO allowed_value (resource_clazz, field_name, value, sku) VALUES
    ('Pearl', 'clazz', 'Pearl', 'P'),
    ('Pearl', 'quantityType', 'Strand', 'S'),
    ('Pearl', 'quantityType', 'Piece', 'P'),
    ('Diamond', 'clazz', 'Diamond', 'D'),
    ('Diamond', 'quantityType', 'Piece', ''),
    ('Diamond', 'type', 'Natural', 'Nat'),
    ('Diamond', 'type', 'Lab Grown', 'Lab'),
    ('DiamondMelee', 'clazz', 'DiamondMelee', 'D'),
    ('DiamondMelee', 'quantityType', 'Piece', 'M'),
    ('DiamondMelee', 'type', 'Natural', 'Nat'),
    ('DiamondMelee', 'type', 'Lab Grown', 'Lab'),
    ('ColoredStone', 'clazz', 'ColoredStone', 'CS'),
    ('ColoredStone', 'quantityType', 'Piece', ''),
    ('ColoredStone', 'type', 'Sapphire', 'Sp'),
    ('ColoredStone', 'type', 'Ruby', 'Ru'),
    ('ColoredStone', 'type', 'Emerald', 'Em'),
    ('ColoredStoneMelee', 'clazz', 'ColoredStoneMelee', 'CS'),
    ('ColoredStoneMelee', 'quantityType', 'Piece', 'M'),
    ('ColoredStoneMelee', 'type', 'Sapphire', 'Sp'),
    ('ColoredStoneMelee', 'type', 'Ruby', 'Ru'),
    ('ColoredStoneMelee', 'type', 'Emerald', 'Em'),
    ('SemiPreciousStone', 'clazz', 'SemiPreciousStone', 'SP'),
    ('SemiPreciousStone', 'quantityType', 'Strand', 'S'),
    ('SemiPreciousStone', 'quantityType', 'Piece', 'P'),
    ('Metal', 'clazz', 'Metal', 'M'),
    ('Metal', 'quantityType', 'Weight', ''),
    ('Metal', 'type', 'Gold', 'G'),
    ('Metal', 'type', 'Silver', 'S'),
    ('Metal', 'type', 'Platinum', 'P'),
    ('Metal', 'type', 'Other', 'O'),
    ('Element', 'clazz','Element', 'E'),
    ('Element', 'quantityType','Piece', 'P');