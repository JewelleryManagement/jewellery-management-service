UPDATE allowed_value
SET sku = 'test'
WHERE sku IS NULL;

UPDATE public.resource SET sku = 'P.C.0.00' WHERE id = '6ea11215-db8f-4b5a-a5dd-1fa748059655';
UPDATE public.resource SET sku = 'M.C.0' WHERE id = '9f76ddce-b01d-451b-add2-4218fd87d358';
UPDATE public.resource SET sku = 'P.0' WHERE id = '4d3b076c-b8da-48ab-bfd8-fb50a470e922';
UPDATE public.resource SET sku = 'S.C' WHERE id = '00d905ba-836f-4cef-8827-c339ca94367c';
UPDATE public.resource SET sku = 'E.C' WHERE id = 'eeb95277-9d4a-46ea-a876-2f786ae4fd5a';

UPDATE public.resource
SET dtype = 'Diamond',
    clazz = 'Diamond'
WHERE dtype = 'PreciousStone' OR clazz = 'PreciousStone';

UPDATE public.allowed_value
SET resource_clazz = 'Diamond'
WHERE resource_clazz = 'PreciousStone';

UPDATE public.resource
SET dtype = 'DiamondMelee',
    clazz = 'DiamondMelee'
WHERE dtype = 'SemiPreciousStone' OR clazz = 'SemiPreciousStone';

UPDATE public.allowed_value
SET resource_clazz = 'DiamondMelee'
WHERE resource_clazz = 'SemiPreciousStone';
