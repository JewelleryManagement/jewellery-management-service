INSERT INTO public.organizations(id,name, address, note) VALUES ('640809ce-f04b-46c3-9a01-9cd33034d185','Organization with User, Sale and Resources', 'Test Street', 'This is the Organization with User and Sale and Resources.');

INSERT INTO public.organizations(id,name, address, note) VALUES ('c676d048-d0ae-40a8-b5bd-cffcf2095cb2','Organization', '456 Oak Avenue', 'This is the second organization.');

INSERT INTO public.resource_in_organization (id,organization_id, resource_id, quantity)
VALUES ('d780e221-82bd-4766-974d-be2b760238b3','640809ce-f04b-46c3-9a01-9cd33034d185', '00d905ba-836f-4cef-8827-c339ca94367c', 1000);

INSERT INTO public.user_in_organization (id, user_id, organization_id, organization_permission)
VALUES ('28a8608d-9b78-414f-b7a0-80aa0cc5cfc5', '88596531-7f0f-407d-b502-31833b8c8e8d', '640809ce-f04b-46c3-9a01-9cd33034d185', ARRAY['DESTROY_ORGANIZATION', 'MANAGE_USERS', 'ADD_RESOURCE_QUANTITY', 'REMOVE_RESOURCE_QUANTITY', 'CREATE_PRODUCT', 'EDIT_PRODUCT', 'DISASSEMBLE_PRODUCT', 'TRANSFER_PRODUCT', 'CREATE_SALE', 'RETURN_RESOURCE', 'RETURN_PRODUCT', 'TRANSFER_RESOURCE']);
