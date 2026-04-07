-- Drop tables if they already exist (safe for dev environments)
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    balance DOUBLE PRECISION DEFAULT 0.0
);

-- Create transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL, -- "DEPOSIT" or "WITHDRAW"
    amount DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

-- Index for faster lookups
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);