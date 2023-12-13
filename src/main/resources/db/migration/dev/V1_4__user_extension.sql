-- Rename 'username' to 'first_name'
ALTER TABLE users
RENAME COLUMN name TO first_name;

-- Add 'last_name' column
ALTER TABLE users
ADD COLUMN last_name VARCHAR(50);

-- Add 'address' column
ALTER TABLE users
ADD COLUMN address VARCHAR(100);

-- Add 'phone' column
ALTER TABLE users
ADD COLUMN phone VARCHAR(20);

-- Add 'phone2' column
ALTER TABLE users
ADD COLUMN phone2 VARCHAR(20);

-- Add 'birth_date' column
ALTER TABLE users
ADD COLUMN birth_date DATE;

-- Add 'note' column
ALTER TABLE users
ADD COLUMN note VARCHAR(500);

UPDATE users
SET last_name = 'test'
WHERE id IN ('88596531-7f0f-407d-b502-31833b8c8e8d', 'beb48c43-cb43-4238-9442-74cda523ed81', '97230978-ac6f-4153-964d-b027e791cb7f', '87230978-ac6f-4153-964d-b027e791cb7f');

