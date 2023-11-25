INSERT INTO public.users
(id, email, name, password, role)
SELECT 'beb48c43-cb43-4238-9442-74cda523ed81', 'testUser1@gmail.com', 'testUser1', '$2a$10$n08gsRFh4ifnR0icGNFbsuEhwslkyp01yKAI6NrfScvvRREys79dm', 'USER'
WHERE
NOT EXISTS (
SELECT id FROM users WHERE id = 'beb48c43-cb43-4238-9442-74cda523ed81'
);
INSERT INTO public.users
(id, email, name, password, role)
SELECT '97230978-ac6f-4153-964d-b027e791cb7f', 'testUser2@gmail.com', 'testUser2', '$2a$10$6VIy9SpsWZJJoV8PcJL64OFW97EDRMMHrlrOdQIuyCgVakfvNlQjC', 'USER'
WHERE
NOT EXISTS (
SELECT id FROM users WHERE id = '97230978-ac6f-4153-964d-b027e791cb7f'
);
INSERT INTO public.users
(id, email, name, password, role)
SELECT '87230978-ac6f-4153-964d-b027e791cb7f', 'admin@gmail.com', 'admin', '$2a$10$6VIy9SpsWZJJoV8PcJL64OFW97EDRMMHrlrOdQIuyCgVakfvNlQjC', 'ADMIN'
WHERE
NOT EXISTS (
SELECT id FROM users WHERE id = '87230978-ac6f-4153-964d-b027e791cb7f'
);


INSERT INTO public.resource
(dtype, id, clazz, note, price_per_quantity, quantity_type, description, color, plating, purity, "type", quality, shape, "size", carat, clarity, cut, dimensionx, dimensiony, dimensionz)
SELECT 'Pearl', '6ea11215-db8f-4b5a-a5dd-1fa748059655', 'Pearl', NULL, 0.0, 'Carat', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.0, NULL, NULL, NULL, NULL, NULL, NULL
WHERE NOT EXISTS (
SELECT id FROM resource WHERE id = '6ea11215-db8f-4b5a-a5dd-1fa748059655');
INSERT INTO public.resource
(dtype, id, clazz, note, price_per_quantity, quantity_type, description, color, plating, purity, "type", quality, shape, "size", carat, clarity, cut, dimensionx, dimensiony, dimensionz)
SELECT 'Metal', '9f76ddce-b01d-451b-add2-4218fd87d358'::uuid, 'Metal', NULL, 0.0, 'Carat', NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL
WHERE NOT EXISTS (
SELECT id FROM resource WHERE id = '9f76ddce-b01d-451b-add2-4218fd87d358');
INSERT INTO public.resource
(dtype, id, clazz, note, price_per_quantity, quantity_type, description, color, plating, purity, "type", quality, shape, "size", carat, clarity, cut, dimensionx, dimensiony, dimensionz)
SELECT 'PreciousStone', '4d3b076c-b8da-48ab-bfd8-fb50a470e922', 'PreciousStone', NULL, 0.0, 'Carat', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.0, NULL, NULL, 0.0, 0.0, 0.0
WHERE NOT EXISTS (
SELECT id FROM resource WHERE id = '4d3b076c-b8da-48ab-bfd8-fb50a470e922');
INSERT INTO public.resource
(dtype, id, clazz, note, price_per_quantity, quantity_type, description, color, plating, purity, "type", quality, shape, "size", carat, clarity, cut, dimensionx, dimensiony, dimensionz)
SELECT 'SemiPreciousStone', '00d905ba-836f-4cef-8827-c339ca94367c', 'SemiPreciousStone', NULL, 0.0, 'Carat', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL
WHERE NOT EXISTS (
SELECT id FROM resource WHERE id = '00d905ba-836f-4cef-8827-c339ca94367c');
INSERT INTO public.resource
(dtype, id, clazz, note, price_per_quantity, quantity_type, description, color, plating, purity, "type", quality, shape, "size", carat, clarity, cut, dimensionx, dimensiony, dimensionz)
SELECT 'Element', 'eeb95277-9d4a-46ea-a876-2f786ae4fd5a', 'Element', NULL, 0.0, 'Carat', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL
WHERE NOT EXISTS (
SELECT id FROM resource WHERE id = 'eeb95277-9d4a-46ea-a876-2f786ae4fd5a');


INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT '1711f345-620a-4095-8c7b-51d6e61f4b69', 6.0, '88596531-7f0f-407d-b502-31833b8c8e8d', 'eeb95277-9d4a-46ea-a876-2f786ae4fd5a'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = '1711f345-620a-4095-8c7b-51d6e61f4b69');
INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT 'a9341e34-85a2-4fb0-b9bc-79b0e70cf35f', 9.0, '88596531-7f0f-407d-b502-31833b8c8e8d', '00d905ba-836f-4cef-8827-c339ca94367c'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = 'a9341e34-85a2-4fb0-b9bc-79b0e70cf35f');
INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT '1eb4d42d-1f81-43f5-8072-3950caa51a0a', 5.0, '88596531-7f0f-407d-b502-31833b8c8e8d', '9f76ddce-b01d-451b-add2-4218fd87d358'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = '1eb4d42d-1f81-43f5-8072-3950caa51a0a');
INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT '2186036c-a4e5-41a9-b941-c16a20d79e10', 12.0, '88596531-7f0f-407d-b502-31833b8c8e8d', '6ea11215-db8f-4b5a-a5dd-1fa748059655'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = '2186036c-a4e5-41a9-b941-c16a20d79e10');
INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT '2bde4aba-ac70-4006-81a6-ebd37108be5d', 5.0, '88596531-7f0f-407d-b502-31833b8c8e8d', '4d3b076c-b8da-48ab-bfd8-fb50a470e922'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = '2bde4aba-ac70-4006-81a6-ebd37108be5d');
INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT 'bf1d46e3-fe75-4497-a561-421ba6d0d95b', 20.0, '97230978-ac6f-4153-964d-b027e791cb7f', '6ea11215-db8f-4b5a-a5dd-1fa748059655'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = 'bf1d46e3-fe75-4497-a561-421ba6d0d95b');
INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT 'e06e3c99-9845-46e2-ba0d-f149b99824a8', 20.0, '97230978-ac6f-4153-964d-b027e791cb7f', '9f76ddce-b01d-451b-add2-4218fd87d358'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = 'e06e3c99-9845-46e2-ba0d-f149b99824a8');
INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT 'bff8a292-5eec-4071-99c8-92d5c4ba392b', 20.0, '97230978-ac6f-4153-964d-b027e791cb7f', '4d3b076c-b8da-48ab-bfd8-fb50a470e922'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = 'bff8a292-5eec-4071-99c8-92d5c4ba392b');
INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT '3454e598-b232-479f-954b-2f46f42a0207', 24.0, 'beb48c43-cb43-4238-9442-74cda523ed81', '4d3b076c-b8da-48ab-bfd8-fb50a470e922'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = '3454e598-b232-479f-954b-2f46f42a0207');
INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT 'afccc4e8-5fc9-4868-a9e4-f980e05ea944', 12.0, 'beb48c43-cb43-4238-9442-74cda523ed81', '9f76ddce-b01d-451b-add2-4218fd87d358'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = 'afccc4e8-5fc9-4868-a9e4-f980e05ea944');
INSERT INTO public.resource_in_user
(id, quantity, owner_id, resource_id)
SELECT '3f704ed4-11aa-4277-8185-5bfac6819276', 24.0, 'beb48c43-cb43-4238-9442-74cda523ed81', 'eeb95277-9d4a-46ea-a876-2f786ae4fd5a'
WHERE NOT EXISTS (
SELECT id FROM resource_in_user WHERE id = '3f704ed4-11aa-4277-8185-5bfac6819276');


INSERT INTO public.product
(id, catalog_number, description, discount, production_number, sale_price, content_of, image_id, owner_id, part_of_sale_id)
SELECT '3c552c1f-9a96-445a-aa36-b772a6c2c113', '1', 'This is my product', 0.0, '1111', 15000.0, NULL, NULL, '88596531-7f0f-407d-b502-31833b8c8e8d', NULL
WHERE NOT EXISTS (
SELECT id FROM product WHERE id = '3c552c1f-9a96-445a-aa36-b772a6c2c113');
INSERT INTO public.product_author
(product_id, user_id)
SELECT '3c552c1f-9a96-445a-aa36-b772a6c2c113', 'beb48c43-cb43-4238-9442-74cda523ed81'
WHERE NOT EXISTS (
SELECT product_id FROM product_author WHERE product_id = '3c552c1f-9a96-445a-aa36-b772a6c2c113');
INSERT INTO public.resource_in_product
(id, quantity, product_id, resource_id)
SELECT 'b365762e-b400-44dc-9d91-6f3ed3aab363', 5.0, '3c552c1f-9a96-445a-aa36-b772a6c2c113', '4d3b076c-b8da-48ab-bfd8-fb50a470e922'
WHERE NOT EXISTS (
SELECT id FROM resource_in_product WHERE id = 'b365762e-b400-44dc-9d91-6f3ed3aab363');

INSERT INTO public.product
(id, catalog_number, description, discount, production_number, sale_price, content_of, image_id, owner_id, part_of_sale_id)
SELECT 'e0f72130-f83d-4ca7-845b-aa82f2bb9a22', '2', 'This is my product', 0.0, '2222', 10000.0, NULL, NULL, '88596531-7f0f-407d-b502-31833b8c8e8d', NULL
WHERE NOT EXISTS (
SELECT id FROM product WHERE id = 'e0f72130-f83d-4ca7-845b-aa82f2bb9a22');
INSERT INTO public.product_author
(product_id, user_id)
SELECT 'e0f72130-f83d-4ca7-845b-aa82f2bb9a22', 'beb48c43-cb43-4238-9442-74cda523ed81'
WHERE NOT EXISTS (
SELECT product_id FROM product_author WHERE product_id = 'e0f72130-f83d-4ca7-845b-aa82f2bb9a22');
INSERT INTO public.resource_in_product
(id, quantity, product_id, resource_id)
SELECT 'c4f51eee-e5b6-4b0f-94c8-76c939b7dbb4', 2.0, 'e0f72130-f83d-4ca7-845b-aa82f2bb9a22', '9f76ddce-b01d-451b-add2-4218fd87d358'
WHERE NOT EXISTS (
SELECT id FROM resource_in_product WHERE id = 'c4f51eee-e5b6-4b0f-94c8-76c939b7dbb4');
INSERT INTO public.resource_in_product
(id, quantity, product_id, resource_id)
SELECT '2e852f6b-d17d-4c2d-9fed-e3467b1f6398', 3.0, 'e0f72130-f83d-4ca7-845b-aa82f2bb9a22', 'eeb95277-9d4a-46ea-a876-2f786ae4fd5a'
WHERE NOT EXISTS (
SELECT id FROM resource_in_product WHERE id = '2e852f6b-d17d-4c2d-9fed-e3467b1f6398');

INSERT INTO public.product
(id, catalog_number, description, discount, production_number, sale_price, content_of, image_id, owner_id, part_of_sale_id)
SELECT 'da284aef-9bf2-4e26-8bb7-827c8afb8cc8', '3', 'This is my product with product', 0.0, '3333', 80000.0, NULL, NULL, '88596531-7f0f-407d-b502-31833b8c8e8d', NULL
WHERE NOT EXISTS (
SELECT id FROM product WHERE id = 'da284aef-9bf2-4e26-8bb7-827c8afb8cc8');
INSERT INTO public.product_author
(product_id, user_id)
SELECT 'da284aef-9bf2-4e26-8bb7-827c8afb8cc8', 'beb48c43-cb43-4238-9442-74cda523ed81'
WHERE NOT EXISTS (
SELECT product_id FROM product_author WHERE product_id = 'da284aef-9bf2-4e26-8bb7-827c8afb8cc8');
INSERT INTO public.resource_in_product
(id, quantity, product_id, resource_id)
SELECT '6c25e204-315d-4fad-bf5b-2637ef9582ec', 3.0, 'da284aef-9bf2-4e26-8bb7-827c8afb8cc8', '9f76ddce-b01d-451b-add2-4218fd87d358'
WHERE NOT EXISTS (
SELECT id FROM resource_in_product WHERE id = '6c25e204-315d-4fad-bf5b-2637ef9582ec');
UPDATE public.product
SET content_of = 'da284aef-9bf2-4e26-8bb7-827c8afb8cc8'
WHERE id = '3c552c1f-9a96-445a-aa36-b772a6c2c113';

INSERT INTO public.product
(id, catalog_number, description, discount, production_number, sale_price, content_of, image_id, owner_id, part_of_sale_id)
SELECT 'b17a976f-ebba-4142-a52e-5fd08bc7f8cd', '4', 'This is my best test product', 0.0, '4444', 4500.0, NULL, NULL, 'beb48c43-cb43-4238-9442-74cda523ed81', NULL
WHERE NOT EXISTS (
SELECT id FROM product WHERE id = 'b17a976f-ebba-4142-a52e-5fd08bc7f8cd');
INSERT INTO public.product_author
(product_id, user_id)
SELECT 'b17a976f-ebba-4142-a52e-5fd08bc7f8cd', '87230978-ac6f-4153-964d-b027e791cb7f'
WHERE NOT EXISTS (
SELECT product_id FROM product_author WHERE product_id = 'b17a976f-ebba-4142-a52e-5fd08bc7f8cd');
INSERT INTO public.resource_in_product
(id, quantity, product_id, resource_id)
SELECT '1e5f5ad4-b0f0-4f78-afc2-40abe74fd05d', 4.0, 'b17a976f-ebba-4142-a52e-5fd08bc7f8cd', '4d3b076c-b8da-48ab-bfd8-fb50a470e922'
WHERE NOT EXISTS (
SELECT id FROM resource_in_product WHERE id = '1e5f5ad4-b0f0-4f78-afc2-40abe74fd05d');

INSERT INTO public.image
(id, file_path, type)
SELECT '269cdb27-2864-4136-ad46-aea94030db04', '\tmp\Jms\Images\3c552c1f-9a96-445a-aa36-b772a6c2c113\ProductPicture.jpeg', 'image/jpeg'
WHERE NOT EXISTS (
SELECT id FROM image WHERE id = '269cdb27-2864-4136-ad46-aea94030db04');
UPDATE public.product
SET image_id = '269cdb27-2864-4136-ad46-aea94030db04'
WHERE id = '3c552c1f-9a96-445a-aa36-b772a6c2c113';

INSERT INTO public.image
(id, file_path, type)
SELECT '333cdb27-2864-4136-ad46-aea94030db04', '\tmp\Jms\Images\da284aef-9bf2-4e26-8bb7-827c8afb8cc8\ProductPicture.jpeg', 'image/jpeg'
WHERE NOT EXISTS (
SELECT id FROM image WHERE id = '333cdb27-2864-4136-ad46-aea94030db04');
UPDATE public.product
SET image_id = '333cdb27-2864-4136-ad46-aea94030db04'
WHERE id = 'da284aef-9bf2-4e26-8bb7-827c8afb8cc8';


INSERT INTO public.sale
(id, "date", buyer_id, seller_id)
SELECT '38839f01-ed22-4a15-86eb-125d7cceafd9', '2023-11-11 02:00:00.000', 'beb48c43-cb43-4238-9442-74cda523ed81', '88596531-7f0f-407d-b502-31833b8c8e8d'
WHERE NOT EXISTS (
SELECT id FROM sale WHERE id = '38839f01-ed22-4a15-86eb-125d7cceafd9');
UPDATE public.product
SET part_of_sale_id = '38839f01-ed22-4a15-86eb-125d7cceafd9'
WHERE id = 'e0f72130-f83d-4ca7-845b-aa82f2bb9a22';


INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '89cde156-5535-4d9f-90d3-7b3744980635', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entity": {"id": "33d0bc3f-0596-4541-9f1e-f89e7ef2c615", "name": "testUser3", "email": "testUser3@gmail.com"}}', '2023-11-25 12:55:43.361', 'USER_CREATE'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '89cde156-5535-4d9f-90d3-7b3744980635');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '00f81473-672c-42e9-a3a6-dfabb2c09d53', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entity": {"id": "0c57ade4-ee07-4373-8823-caeb0c781e94", "note": null, "size": 0.0, "type": null, "clazz": "Pearl", "color": null, "shape": null, "quality": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}', '2023-11-25 12:56:01.446', 'RESOURCE_CREATE'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '00f81473-672c-42e9-a3a6-dfabb2c09d53');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '8a8f8371-3d13-42c5-ab9c-a478a8f4cf16', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entity": {"id": "ce9a3035-488a-42b5-9c24-6f17e958b095", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}', '2023-11-25 12:57:16.925', 'RESOURCE_CREATE'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '8a8f8371-3d13-42c5-ab9c-a478a8f4cf16');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '35999650-1872-466f-b5b7-21bc0bf91354', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entity": {"id": "0454f706-f599-4997-b0ad-54034b53e5fd", "note": null, "clazz": "Element", "description": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}', '2023-11-25 12:57:22.060', 'RESOURCE_CREATE'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '35999650-1872-466f-b5b7-21bc0bf91354');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '1ada7745-441e-4a67-8c2c-4ae37ab34699', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entity": {"id": "f687192e-6f3b-42ad-9016-350138892d24", "cut": null, "note": null, "size": "0.00x0.00x0.00", "carat": 0.0, "clazz": "PreciousStone", "color": null, "shape": null, "clarity": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}', '2023-11-25 12:57:39.329', 'RESOURCE_CREATE'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '1ada7745-441e-4a67-8c2c-4ae37ab34699');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '40da9dba-79b2-4a20-a14e-04f81a68a3af', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entity": {"id": "63e21867-a394-4f81-b37f-2e8e00e57b3f", "cut": null, "note": null, "size": null, "clazz": "SemiPreciousStone", "color": null, "shape": null, "clarity": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}', '2023-11-25 12:57:45.708', 'RESOURCE_CREATE'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '40da9dba-79b2-4a20-a14e-04f81a68a3af');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT 'b5bd6db6-8465-47f2-ae78-6658d7359fda', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entityAfter": {"owner": {"id": "33d0bc3f-0596-4541-9f1e-f89e7ef2c615", "name": "testUser3", "email": "testUser3@gmail.com"}, "resourcesAndQuantities": [{"quantity": 20.0, "resource": {"id": "0c57ade4-ee07-4373-8823-caeb0c781e94", "note": null, "size": 0.0, "type": null, "clazz": "Pearl", "color": null, "shape": null, "quality": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}, "entityBefore": null}', '2023-11-25 12:58:58.713', 'RESOURCE_ADD_QUANTITY'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = 'b5bd6db6-8465-47f2-ae78-6658d7359fda');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '1494f6c8-e20a-45c3-9598-6d331206dfce', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entityAfter": {"owner": {"id": "33d0bc3f-0596-4541-9f1e-f89e7ef2c615", "name": "testUser3", "email": "testUser3@gmail.com"}, "resourcesAndQuantities": [{"quantity": 20.0, "resource": {"id": "ce9a3035-488a-42b5-9c24-6f17e958b095", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}, "entityBefore": null}', '2023-11-25 12:59:09.973', 'RESOURCE_ADD_QUANTITY'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '1494f6c8-e20a-45c3-9598-6d331206dfce');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT 'afefaa66-0e2c-4dbc-b791-f4e981baa0ed', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entityAfter": {"owner": {"id": "33d0bc3f-0596-4541-9f1e-f89e7ef2c615", "name": "testUser3", "email": "testUser3@gmail.com"}, "resourcesAndQuantities": [{"quantity": 20.0, "resource": {"id": "0454f706-f599-4997-b0ad-54034b53e5fd", "note": null, "clazz": "Element", "description": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}, "entityBefore": null}', '2023-11-25 12:59:19.113', 'RESOURCE_ADD_QUANTITY'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = 'afefaa66-0e2c-4dbc-b791-f4e981baa0ed');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '87b8b068-564e-472c-b602-7e5d98d86256', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entityAfter": {"owner": {"id": "33d0bc3f-0596-4541-9f1e-f89e7ef2c615", "name": "testUser3", "email": "testUser3@gmail.com"}, "resourcesAndQuantities": [{"quantity": 20.0, "resource": {"id": "f687192e-6f3b-42ad-9016-350138892d24", "cut": null, "note": null, "size": "0.00x0.00x0.00", "carat": 0.0, "clazz": "PreciousStone", "color": null, "shape": null, "clarity": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}, "entityBefore": null}', '2023-11-25 12:59:32.936', 'RESOURCE_ADD_QUANTITY'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '87b8b068-564e-472c-b602-7e5d98d86256');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '1a00592c-107a-4839-8766-f1823123422b', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entityAfter": {"owner": {"id": "33d0bc3f-0596-4541-9f1e-f89e7ef2c615", "name": "testUser3", "email": "testUser3@gmail.com"}, "resourcesAndQuantities": [{"quantity": 20.0, "resource": {"id": "63e21867-a394-4f81-b37f-2e8e00e57b3f", "cut": null, "note": null, "size": null, "clazz": "SemiPreciousStone", "color": null, "shape": null, "clarity": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}, "entityBefore": null}', '2023-11-25 12:59:41.611', 'RESOURCE_ADD_QUANTITY'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '1a00592c-107a-4839-8766-f1823123422b');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT 'e09df4c2-c796-4f5f-81df-86bdefdf52b8', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entity": {"id": "a7a9086d-fddb-421a-a0be-0bd95e7298ce", "owner": {"id": "33d0bc3f-0596-4541-9f1e-f89e7ef2c615", "name": "testUser3", "email": "testUser3@gmail.com"}, "authors": [{"id": "33d0bc3f-0596-4541-9f1e-f89e7ef2c615", "name": "testUser3", "email": "testUser3@gmail.com"}], "discount": 0.0, "contentOf": null, "salePrice": 6500.0, "partOfSale": null, "description": "This is my best test product 2", "catalogNumber": "5", "productsContent": [], "productionNumber": "5555", "resourcesContent": [{"quantity": 5.0, "resource": {"id": "ce9a3035-488a-42b5-9c24-6f17e958b095", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}}', '2023-11-25 13:01:02.507', 'PRODUCT_CREATE'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = 'e09df4c2-c796-4f5f-81df-86bdefdf52b8');
INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '7acc1251-d37b-4ad6-90ce-13b878f2b49f', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entity": {"id": "fc79d78c-fc85-41ee-ac0a-112677f90399", "date": "2023-11-11", "buyer": {"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}, "seller": {"id": "33d0bc3f-0596-4541-9f1e-f89e7ef2c615", "name": "testUser3", "email": "testUser3@gmail.com"}, "products": [{"id": "a7a9086d-fddb-421a-a0be-0bd95e7298ce", "owner": {"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}, "authors": [{"id": "33d0bc3f-0596-4541-9f1e-f89e7ef2c615", "name": "testUser3", "email": "testUser3@gmail.com"}], "discount": 5.0, "contentOf": null, "salePrice": 4500.0, "partOfSale": "fc79d78c-fc85-41ee-ac0a-112677f90399", "description": "This is my best test product 2", "catalogNumber": "5", "productsContent": [], "productionNumber": "5555", "resourcesContent": [{"quantity": 5.0, "resource": {"id": "ce9a3035-488a-42b5-9c24-6f17e958b095", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}], "totalPrice": 4500.0, "totalDiscount": 5.0, "totalDiscountedPrice": 4275.0}}', '2023-11-25 13:02:57.641', 'SALE_CREATE'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '7acc1251-d37b-4ad6-90ce-13b878f2b49f');