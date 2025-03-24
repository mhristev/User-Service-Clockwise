INSERT INTO users (id, username, email, password, role)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'admin',
           'admin@wms.com',
           '$2a$10$3Qvn1EZoLvOLIHxmn1qnNu9l8J0U2FrUfv5f.zOjgBk5zGNKPYRXC', -- Bcrypt hash for 'admin123'
           'ADMIN'
       );