ALTER TABLE public.resource
ADD CONSTRAINT resource_sku_unique UNIQUE (sku);