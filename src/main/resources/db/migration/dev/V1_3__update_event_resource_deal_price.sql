
DELETE FROM public.system_event
WHERE id = '89cde156-5535-4d9f-90d3-7b3744980635';

DELETE FROM public.system_event
WHERE id = '00f81473-672c-42e9-a3a6-dfabb2c09d53';

DELETE FROM public.system_event
WHERE id = '8a8f8371-3d13-42c5-ab9c-a478a8f4cf16';

DELETE FROM public.system_event
WHERE id = '35999650-1872-466f-b5b7-21bc0bf91354';

DELETE FROM public.system_event
WHERE id = '1ada7745-441e-4a67-8c2c-4ae37ab34699';

DELETE FROM public.system_event
WHERE id = '40da9dba-79b2-4a20-a14e-04f81a68a3af';

DELETE FROM public.system_event
WHERE id = 'b5bd6db6-8465-47f2-ae78-6658d7359fda';

DELETE FROM public.system_event
WHERE id = '1494f6c8-e20a-45c3-9598-6d331206dfce';

DELETE FROM public.system_event
WHERE id = 'afefaa66-0e2c-4dbc-b791-f4e981baa0ed';

DELETE FROM public.system_event
WHERE id = '87b8b068-564e-472c-b602-7e5d98d86256';

DELETE FROM public.system_event
WHERE id = '1a00592c-107a-4839-8766-f1823123422b';

DELETE FROM public.system_event
WHERE id = 'e09df4c2-c796-4f5f-81df-86bdefdf52b8';

DELETE FROM public.system_event
WHERE id = '7acc1251-d37b-4ad6-90ce-13b878f2b49f';

INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '4fe2f031-0c2a-4822-9b50-bf3f023df66c', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entityAfter": {"owner": {"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}, "dealPrice": 600.0, "resourcesAndQuantities": [{"quantity": 225.0, "resource": {"id": "9f76ddce-b01d-451b-add2-4218fd87d358", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}, "entityBefore": {"owner": {"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}, "dealPrice": null, "resourcesAndQuantities": [{"quantity": 205.0, "resource": {"id": "9f76ddce-b01d-451b-add2-4218fd87d358", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}}', '2023-12-06 11:15:14.556', 'RESOURCE_ADD_QUANTITY'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '4fe2f031-0c2a-4822-9b50-bf3f023df66c');

INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '4442f031-0c2a-4822-9b50-bf3f023df66c', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entityAfter": {"owner": {"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}, "dealPrice": 1600.25, "resourcesAndQuantities": [{"quantity": 225.0, "resource": {"id": "9f76ddce-b01d-451b-add2-4218fd87d358", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}, "entityBefore": {"owner": {"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}, "dealPrice": null, "resourcesAndQuantities": [{"quantity": 205.0, "resource": {"id": "9f76ddce-b01d-451b-add2-4218fd87d358", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}}', '2023-12-06 11:15:14.556', 'RESOURCE_ADD_QUANTITY'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '4442f031-0c2a-4822-9b50-bf3f023df66c');

INSERT INTO public.system_event
(id, executor, payload, "timestamp", "type")
SELECT '4562f031-0c2a-4822-9b50-bf3f023df66c', '{"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}', '{"entityAfter": {"owner": {"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}, "dealPrice": 2000.45, "resourcesAndQuantities": [{"quantity": 225.0, "resource": {"id": "9f76ddce-b01d-451b-add2-4218fd87d358", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}, "entityBefore": {"owner": {"id": "88596531-7f0f-407d-b502-31833b8c8e8d", "name": "root", "email": "root@gmail.com"}, "dealPrice": null, "resourcesAndQuantities": [{"quantity": 205.0, "resource": {"id": "9f76ddce-b01d-451b-add2-4218fd87d358", "note": null, "type": null, "clazz": "Metal", "color": null, "purity": 0, "plating": null, "quantityType": "Carat", "pricePerQuantity": 0.0}}]}}', '2023-12-06 11:15:14.556', 'RESOURCE_ADD_QUANTITY'
WHERE NOT EXISTS (
SELECT id FROM system_event WHERE id = '4562f031-0c2a-4822-9b50-bf3f023df66c');