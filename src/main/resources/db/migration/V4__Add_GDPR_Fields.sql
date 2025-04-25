-- Add GDPR-related fields to users table

-- Add privacy consent as a JSONB column
ALTER TABLE users ADD COLUMN privacy_consent JSONB DEFAULT NULL;

-- Add data retention date (when user data should be deleted/anonymized)
ALTER TABLE users ADD COLUMN data_retention_date BIGINT DEFAULT NULL;

-- Add consent version to track which version of privacy policy user agreed to
ALTER TABLE users ADD COLUMN consent_version VARCHAR(50) DEFAULT NULL; 