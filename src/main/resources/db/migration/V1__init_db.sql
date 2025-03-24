
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


CREATE TABLE users (
                       id VARCHAR(50) DEFAULT uuid_generate_v4()::text PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       restaurant_id VARCHAR(50)
);

CREATE TABLE refresh_tokens (
                                id VARCHAR(50) DEFAULT uuid_generate_v4()::text PRIMARY KEY,
                                token VARCHAR(255) NOT NULL UNIQUE,
                                user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                expiry_date BIGINT NOT NULL,
                                is_revoked BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);


CREATE INDEX idx_users_restaurant_id ON users(restaurant_id);