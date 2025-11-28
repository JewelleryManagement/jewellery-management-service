ALTER TABLE public.resource
ADD COLUMN shape_specification VARCHAR(255) NULL,
ADD COLUMN color_hue VARCHAR(255) NULL,
ADD COLUMN polish VARCHAR(255) NULL,
ADD COLUMN symmetry VARCHAR(255) NULL,
ADD COLUMN fluorescence VARCHAR(255) NULL,
ADD COLUMN certificate VARCHAR(255) NULL,
ALTER COLUMN size TYPE VARCHAR(255);

ALTER TABLE allowed_value
ADD COLUMN sku VARCHAR(255);

UPDATE allowed_value
SET sku = 'test'
WHERE sku IS NULL;

ALTER TABLE allowed_value
ALTER COLUMN sku SET NOT NULL;

ALTER TABLE allowed_value
DROP CONSTRAINT allowed_value_pkey;

ALTER TABLE allowed_value
ADD CONSTRAINT allowed_value_pkey
PRIMARY KEY (resource_clazz, field_name, value, sku);

ALTER TABLE public.resource
ADD COLUMN sku VARCHAR(255);

UPDATE public.resource SET sku = 'P.C.0.00' WHERE id = '6ea11215-db8f-4b5a-a5dd-1fa748059655';
UPDATE public.resource SET sku = 'M.C.0' WHERE id = '9f76ddce-b01d-451b-add2-4218fd87d358';
UPDATE public.resource SET sku = 'P.0' WHERE id = '4d3b076c-b8da-48ab-bfd8-fb50a470e922';
UPDATE public.resource SET sku = 'S.C' WHERE id = '00d905ba-836f-4cef-8827-c339ca94367c';
UPDATE public.resource SET sku = 'E.C' WHERE id = 'eeb95277-9d4a-46ea-a876-2f786ae4fd5a';

ALTER TABLE public.resource
ALTER COLUMN sku SET NOT NULL;

UPDATE public.resource
SET dtype = 'Diamond',
    clazz = 'Diamond'
WHERE dtype = 'PreciousStone' OR clazz = 'PreciousStone';

UPDATE public.allowed_value
SET resource_clazz = 'Diamond'
WHERE resource_clazz = 'PreciousStone';
