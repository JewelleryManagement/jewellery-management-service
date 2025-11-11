INSERT INTO public.sale
(id, "date", buyer_id, seller_id)
SELECT '5f9ec133-c010-45b0-aa4b-cfeba01a4105', '2024-02-22 00:00:00.000', 'beb48c43-cb43-4238-9442-74cda523ed81', '88596531-7f0f-407d-b502-31833b8c8e8d'
WHERE NOT EXISTS (
SELECT id FROM sale WHERE id = '5f9ec133-c010-45b0-aa4b-cfeba01a4105');

INSERT INTO public.product_price_discount (id,product_id,sale_id,sale_price,discount) VALUES
('0a530e77-8a37-4746-9731-480caa90be65','e0f72130-f83d-4ca7-845b-aa82f2bb9a22','5f9ec133-c010-45b0-aa4b-cfeba01a4105',100.00,20.00);

INSERT INTO public.purchased_resources (id,resource_id,owner_id,quantity,sale_price,discount,part_of_sale_id) VALUES
('dfc0858b-466e-416d-9782-226be7124a3f','4d3b076c-b8da-48ab-bfd8-fb50a470e922','beb48c43-cb43-4238-9442-74cda523ed81',10,200.00,10,'5f9ec133-c010-45b0-aa4b-cfeba01a4105');

INSERT INTO public.system_event (id,"timestamp","type",executor,payload) VALUES
('cd416527-9fd6-4f3b-be81-7d4143217430','2024-03-26 17:09:27.678767+02','SALE_CREATE','{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "email": "root@gmail.com", "lastName": "test", "firstName": "root"}','{"entity": {"id": "5f9ec133-c010-45b0-aa4b-cfeba01a4105", "date": "22/02/2024", "buyer": {"id": "beb48c43-cb43-4238-9442-74cda523ed81", "note": null, "role": "USER", "email": "testUser1@gmail.com", "phone": null, "phone2": null, "address": null, "lastName": "test", "birthDate": null, "firstName": "testUser1"}, "seller": {"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "note": null, "role": "ADMIN", "email": "root@gmail.com", "phone": null, "phone2": null, "address": null, "lastName": "test", "birthDate": null, "firstName": "root"}, "products": [{"id": "e0f72130-f83d-4ca7-845b-aa82f2bb9a22", "owner": {"id": "beb48c43-cb43-4238-9442-74cda523ed81", "note": null, "role": "USER", "email": "testUser1@gmail.com", "phone": null, "phone2": null, "address": null, "lastName": "test", "birthDate": null, "firstName": "testUser1"}, "authors": [{"id": "beb48c43-cb43-4238-9442-74cda523ed81", "note": null, "role": "USER", "email": "testUser1@gmail.com", "phone": null, "phone2": null, "address": null, "lastName": "test", "birthDate": null, "firstName": "testUser1"}], "discount": 20, "contentOf": null, "salePrice": 100.00, "partOfSale": "5f9ec133-c010-45b0-aa4b-cfeba01a4105", "description": "This is my product", "catalogNumber": "2", "additionalPrice": 0.00, "productsContent": [], "productionNumber": "2222", "resourcesContent": [{"quantity": 2.00, "resource": {"id": "9f76ddce-b01d-451b-add2-4218fd87d358", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 20.00}}, {"quantity": 3.00, "resource": {"id": "eeb95277-9d4a-46ea-a876-2f786ae4fd5a", "note": null, "clazz": "Element", "description": null, "quantityType": "Carat", "pricePerQuantity": 20.00}}]}], "resources": [{"discount": 10, "salePrice": 200.00, "resourceAndQuantity": {"quantity": 10, "resource": {"id": "4d3b076c-b8da-48ab-bfd8-fb50a470e922", "cut": null, "note": null, "size": "0.00x0.00x0.00", "carat": 0.00, "clazz": "PreciousStone", "color": null, "shape": null, "clarity": null, "quantityType": "Carat", "pricePerQuantity": 20.00}}}], "totalPrice": 300.00, "totalDiscount": 13.33, "totalDiscountedPrice": 260.00}}');

UPDATE public.product
SET owner_id='beb48c43-cb43-4238-9442-74cda523ed81'::uuid
WHERE id='e0f72130-f83d-4ca7-845b-aa82f2bb9a22'::uuid;

UPDATE public.resource_in_user
SET quantity=90.00
WHERE id='2bde4aba-ac70-4006-81a6-ebd37108be5d'::uuid;

