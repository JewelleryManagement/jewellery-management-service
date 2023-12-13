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