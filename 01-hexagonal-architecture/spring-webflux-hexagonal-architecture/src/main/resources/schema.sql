CREATE TABLE IF NOT EXISTS "orders" (
    "id" UUID PRIMARY KEY,
    "location" VARCHAR(20) NOT NULL,
    "items" VARCHAR(4000) NOT NULL,
    "status" VARCHAR(20) NOT NULL
);
