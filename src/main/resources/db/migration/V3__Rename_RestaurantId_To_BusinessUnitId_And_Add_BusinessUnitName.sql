ALTER TABLE users RENAME COLUMN restaurant_id TO business_unit_id;
ALTER TABLE users ADD COLUMN business_unit_name VARCHAR(255); 