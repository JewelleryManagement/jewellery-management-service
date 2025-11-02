INSERT INTO users
(id, name, email, password, role)
SELECT '88596531-7f0f-407d-b502-31833b8c8e8d', 'root', 'root@gmail.com', '$2a$12$fGuoN79WFwHPUmirHOlxIO9kdmMTBrlNGKob0ay4muxXNDePg38ri', 'ADMIN'
WHERE
NOT EXISTS (
SELECT id FROM users WHERE id = '88596531-7f0f-407d-b502-31833b8c8e8d'
);