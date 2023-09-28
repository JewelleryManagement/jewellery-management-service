INSERT INTO users (id, name, email, password, role)
VALUES ('88596531-7f0f-407d-b502-31833b8c8e8d', 'root', 'root@gmail.com', '$2a$10$DN49fWbQazilM1zkZEMH2.H2ifiHpqQis2ollx87jdJ5SQLBF6jGm', 'ADMIN')
ON CONFLICT (email) DO NOTHING;