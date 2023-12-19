ALTER TABLE public.system_event
DROP CONSTRAINT IF EXISTS system_event_type_check;

ALTER TABLE public.system_event
ADD CONSTRAINT system_event_type_check
CHECK (((type)::text = ANY ((ARRAY[
'USER_CREATE'::character varying,
'USER_UPDATE'::character varying,
'USER_DELETE'::character varying,
'RESOURCE_CREATE'::character varying,
'RESOURCE_DELETE'::character varying,
'RESOURCE_UPDATE'::character varying,
'RESOURCE_TRANSFER'::character varying,
'RESOURCE_REMOVE_QUANTITY'::character varying,
'RESOURCE_ADD_QUANTITY'::character varying,
'PRODUCT_CREATE'::character varying,
'PRODUCT_TRANSFER'::character varying,
'PRODUCT_DISASSEMBLY'::character varying,
'SALE_CREATE'::character varying,
'SALE_RETURN_PRODUCT'::character varying,
'PRODUCT_UPDATE'::character varying
])::text[])));