-- Add new user profile fields
ALTER TABLE users ADD COLUMN first_name VARCHAR(255);
ALTER TABLE users ADD COLUMN last_name VARCHAR(255);
ALTER TABLE users ADD COLUMN phone_number VARCHAR(50);

-- Add user activity tracking fields
ALTER TABLE users ADD COLUMN created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000;
ALTER TABLE users ADD COLUMN last_seen_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000;

-- Make sure not to drop username immediately as existing data depends on it
-- First, we'll make these NOT NULL constraints temporary
UPDATE users SET first_name = username WHERE first_name IS NULL;
UPDATE users SET last_name = '' WHERE last_name IS NULL;
UPDATE users SET phone_number = '' WHERE phone_number IS NULL;

-- Make the new columns required
ALTER TABLE users ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE users ALTER COLUMN last_name SET NOT NULL;
ALTER TABLE users ALTER COLUMN phone_number SET NOT NULL;

-- Now we can safely remove the username column
-- It's commented out for now - run this after confirming data migration
-- ALTER TABLE users DROP COLUMN username;

-- Add indexes for common lookup patterns
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone_number ON users(phone_number); 