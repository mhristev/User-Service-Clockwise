-- Remove the username column which was commented out in V5__Update_User_Schema.sql but never executed
ALTER TABLE users DROP COLUMN IF EXISTS username;

-- Remove the data_retention_date column added in V4__Add_GDPR_Fields.sql
ALTER TABLE users DROP COLUMN IF EXISTS data_retention_date;

-- Update any code that relies on the username to use email instead
COMMENT ON TABLE users IS 'User accounts - email is used as the username'; 