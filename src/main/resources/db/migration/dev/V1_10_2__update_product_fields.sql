UPDATE public.product
SET organization_id = '640809ce-f04b-46c3-9a01-9cd33034d185'
WHERE id = '3c552c1f-9a96-445a-aa36-b772a6c2c113';

INSERT INTO public.user_in_organization (id, user_id, organization_id, organization_permission)
VALUES ('0efd91ea-29d5-4483-963c-047fdc51d6f1', 'beb48c43-cb43-4238-9442-74cda523ed81', '640809ce-f04b-46c3-9a01-9cd33034d185', ARRAY['ADD_RESOURCE_QUANTITY','REMOVE_RESOURCE_QUANTITY','DISASSEMBLE_PRODUCT','TRANSFER_PRODUCT','CREATE_SALE','RETURN_RESOURCE','RETURN_PRODUCT','TRANSFER_RESOURCE']);

INSERT INTO public.product (id, catalog_number, description, production_number, content_of, image_id, owner_id, additional_price, organization_id)
VALUES ('7f3c2c8c-6e7c-4b47-9c2e-5a8b9d2f3e61', '30', 'description test', '30', null, null, 'beb48c43-cb43-4238-9442-74cda523ed81', 30, '640809ce-f04b-46c3-9a01-9cd33034d185');

INSERT INTO product_author (product_id, user_id)
VALUES ('7f3c2c8c-6e7c-4b47-9c2e-5a8b9d2f3e61', 'beb48c43-cb43-4238-9442-74cda523ed81');

INSERT INTO public.product (id, catalog_number, description, production_number, content_of, image_id, owner_id, additional_price, organization_id)
VALUES ('d2a14c70-8c94-4f2d-b1f7-3cb65fd20c1a', '40', 'description test', '40', null, null, 'beb48c43-cb43-4238-9442-74cda523ed81', 40, '640809ce-f04b-46c3-9a01-9cd33034d185');

INSERT INTO product_author (product_id, user_id)
VALUES ('d2a14c70-8c94-4f2d-b1f7-3cb65fd20c1a', 'beb48c43-cb43-4238-9442-74cda523ed81');

INSERT INTO public.sale (id, date, buyer_id, seller_id, organization_seller_id)
VALUES ('f71bd98e-a7dd-4fb6-8f3e-02e6ac45333c', '2025-11-13', '88596531-7f0f-407d-b502-31833b8c8e8d', null, '640809ce-f04b-46c3-9a01-9cd33034d185');

INSERT INTO public.product_price_discount (id, product_id, sale_id, sale_price, discount)
VALUES ('e38d5cf4-1d31-4498-befd-c6da4f6e632f', 'd2a14c70-8c94-4f2d-b1f7-3cb65fd20c1a', 'f71bd98e-a7dd-4fb6-8f3e-02e6ac45333c', 40.00, 5);