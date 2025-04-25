-- Add user_status column to the users table
ALTER TABLE users ADD COLUMN user_status VARCHAR(20);

-- Set default value for existing users to 'ACTIVE'
UPDATE users SET user_status = 'ACTIVE' WHERE user_status IS NULL;

-- Make the column NOT NULL after setting default values
ALTER TABLE users ALTER COLUMN user_status SET NOT NULL;

-- Set default value for new records
ALTER TABLE users ALTER COLUMN user_status SET DEFAULT 'ACTIVE';

-- Add an index for faster status-based lookups
CREATE INDEX idx_users_status ON users(user_status);

-- Add a check constraint to ensure only valid status values are used
ALTER TABLE users ADD CONSTRAINT chk_user_status CHECK (
    user_status IN ('ACTIVE', 'BANNED', 'ON_HOLD', 'INACTIVE')
); 