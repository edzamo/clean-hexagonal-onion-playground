CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    discontinued BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS stock_items (
    product_id UUID PRIMARY KEY,
    quantity_on_hand INT NOT NULL
);
