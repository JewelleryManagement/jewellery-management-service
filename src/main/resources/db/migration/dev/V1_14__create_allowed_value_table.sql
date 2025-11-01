-- Create allowed_value table
CREATE TABLE allowed_value (
    resource_clazz VARCHAR(255) NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    PRIMARY KEY (resource_clazz, field_name, value)
);

-- Initial entries for Metal
INSERT INTO allowed_value (resource_clazz, field_name, value) VALUES
    ('Metal', 'type', 'gold'),
    ('Metal', 'type', 'silver'),
    ('Metal', 'purity', '585'),
    ('Metal', 'purity', '750'),
    ('Metal', 'color', 'yellow'),
    ('Metal', 'color', 'white'),
    ('Metal', 'plating', 'rhodium'),
    ('Metal', 'plating', 'none');

-- Initial entries for Pearl
INSERT INTO allowed_value (resource_clazz, field_name, value) VALUES
    ('Pearl', 'type', 'akoya'),
    ('Pearl', 'type', 'freshwater'),
    ('Pearl', 'quality', 'AAA'),
    ('Pearl', 'quality', 'AA+'),
    ('Pearl', 'quality', 'AA'),
    ('Pearl', 'quality', 'A+'),
    ('Pearl', 'quality', 'B'),
    ('Pearl', 'color', 'white'),
    ('Pearl', 'color', 'black'),
    ('Pearl', 'color', 'pink'),
    ('Pearl', 'shape', 'round'),
    ('Pearl', 'shape', 'baroque');

-- Initial entries for PreciousStone
INSERT INTO allowed_value (resource_clazz, field_name, value) VALUES
    ('PreciousStone', 'color', 'red'),
    ('PreciousStone', 'color', 'blue'),
    ('PreciousStone', 'cut', 'brilliant'),
    ('PreciousStone', 'cut', 'emerald'),
    ('PreciousStone', 'clarity', 'VS1'),
    ('PreciousStone', 'clarity', 'SI1'),
    ('PreciousStone', 'shape', 'round'),
    ('PreciousStone', 'shape', 'oval');

-- Initial entries for SemiPreciousStone
INSERT INTO allowed_value (resource_clazz, field_name, value) VALUES
    ('SemiPreciousStone', 'color', 'green'),
    ('SemiPreciousStone', 'color', 'purple'),
    ('SemiPreciousStone', 'cut', 'cabochon'),
    ('SemiPreciousStone', 'clarity', 'eye clean'),
    ('SemiPreciousStone', 'shape', 'pear'),
    ('SemiPreciousStone', 'shape', 'cushion');

-- Initial entries for Element (only description field, example value)
INSERT INTO allowed_value (resource_clazz, field_name, value) VALUES
    ('Element', 'description', 'example description'); 