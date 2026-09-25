CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    holder_name VARCHAR(200) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    related_account_id UUID,
    occurred_at TIMESTAMP NOT NULL
);
