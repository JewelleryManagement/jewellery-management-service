INSERT INTO public.organizations(id,name, address, note) VALUES ('640809ce-f04b-46c3-9a01-9cd33034d185','Organization with User, Sale and Resources', 'Test Street', 'This is the Organization with User and Sale and Resources.');

INSERT INTO public.organizations(id,name, address, note) VALUES ('c676d048-d0ae-40a8-b5bd-cffcf2095cb2','Organization', 'Test Street 2', 'This is the second organization.');

INSERT INTO public.resource_in_organization (id,organization_id, resource_id, quantity)
VALUES ('d780e221-82bd-4766-974d-be2b760238b3','640809ce-f04b-46c3-9a01-9cd33034d185', '00d905ba-836f-4cef-8827-c339ca94367c', 1000);

INSERT INTO public.user_in_organization (id, user_id, organization_id, organization_permission)
VALUES ('28a8608d-9b78-414f-b7a0-80aa0cc5cfc5', '88596531-7f0f-407d-b502-31833b8c8e8d', '640809ce-f04b-46c3-9a01-9cd33034d185', ARRAY['DESTROY_ORGANIZATION', 'MANAGE_USERS', 'ADD_RESOURCE_QUANTITY', 'REMOVE_RESOURCE_QUANTITY', 'CREATE_PRODUCT', 'EDIT_PRODUCT', 'DISASSEMBLE_PRODUCT', 'TRANSFER_PRODUCT', 'CREATE_SALE', 'RETURN_RESOURCE', 'RETURN_PRODUCT', 'TRANSFER_RESOURCE']);

INSERT INTO public.product (id, catalog_number, description, production_number, content_of, image_id, owner_id, additional_price, organization_id)
VALUES ('aed68f8c-f501-401d-9747-cabc050e32a8', '99', 'Sold Product from Organization', '99', null, null, null, 10, '640809ce-f04b-46c3-9a01-9cd33034d185');

INSERT INTO public.product (id, catalog_number, description, production_number, content_of, image_id, owner_id, additional_price, organization_id)
VALUES ('353bd632-d7cf-4535-b9ea-76834ad2fbb9', '99', 'description test', '99', null, null, null, 10, '640809ce-f04b-46c3-9a01-9cd33034d185');

INSERT INTO public.resource_in_product (id, quantity, product_id, resource_id)
VALUES ('17692dff-8593-4311-9188-3c1301011c7f', 10, 'aed68f8c-f501-401d-9747-cabc050e32a8', '00d905ba-836f-4cef-8827-c339ca94367c');

INSERT INTO public.sale (id, date, buyer_id, seller_id, organization_seller_id)
VALUES ('fcf943ef-2faf-47d7-b5b5-5bb332df5bbf', '2024-04-26', '97230978-ac6f-4153-964d-b027e791cb7f', null, '640809ce-f04b-46c3-9a01-9cd33034d185');

INSERT INTO public.product_price_discount (id, product_id, sale_id, sale_price, discount)
VALUES ('aad2aaab-88f8-44c3-98d1-58bbbc278354', 'aed68f8c-f501-401d-9747-cabc050e32a8', 'fcf943ef-2faf-47d7-b5b5-5bb332df5bbf', 50.00, 1);

UPDATE public.product
SET owner_id = '97230978-ac6f-4153-964d-b027e791cb7f',
    organization_id = null
WHERE id = 'aed68f8c-f501-401d-9747-cabc050e32a8';
