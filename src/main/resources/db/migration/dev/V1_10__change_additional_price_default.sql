UPDATE public.product
SET additional_price = COALESCE(additional_price, 0);

ALTER TABLE public.product
ALTER COLUMN additional_price SET NOT NULL;
