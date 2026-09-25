-- =================================================================
-- Script de datos iniciales para la base de datos 'coffee-shop'
-- Modelo de datos con UUIDs para identificadores públicos.
-- =================================================================

-- Se eliminan las tablas en orden inverso para evitar problemas con las claves foráneas.
-- Esto hace que el script sea idempotente (se puede ejecutar varias veces sin errores).
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;

-- Creación de la tabla 'orders' con una columna 'uuid' para el identificador público.
-- El 'id' se mantiene como clave primaria interna para un rendimiento óptimo en las relaciones.
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    version BIGINT NOT NULL DEFAULT 0,
    order_date DATETIME NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    location VARCHAR(50) NOT NULL
);

-- Se modifica la tabla 'order_items' para almacenar los detalles de cada línea de pedido,
-- coincidiendo con el 'LineItem' del dominio.
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    drink VARCHAR(50) NOT NULL,
    milk VARCHAR(50) NOT NULL,
    size VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Creación de la tabla 'payments'. Referencia la orden por su UUID público (order_uuid -> orders.uuid),
-- igual que PaymentJpaEntity. Nunca se guarda el número completo de tarjeta: solo last4.
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_uuid VARCHAR(36) NOT NULL UNIQUE,
    last4 VARCHAR(4) NOT NULL,
    card_holder_name VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (order_uuid) REFERENCES orders(uuid)
);

-- Estados de orden = estados del dominio: PAYMENT_EXPECTED, PAID, PREPARING, READY, TAKEN (no existe CANCELLED:
-- cancelar una orden la elimina).

-- Pedido 1: Un pedido para tomar en tienda, que ya ha sido pagado.
INSERT INTO orders (id, uuid, order_date, total_amount, status, location)
VALUES (1, 'f47ac10b-58cc-4372-a567-0e02b2c3d479', '2024-05-21 10:30:00', 9.50, 'PAID', 'IN_STORE');
INSERT INTO order_items (order_id, drink, milk, size, quantity) VALUES (1, 'CAPPUCCINO', 'WHOLE', 'LARGE', 1);
-- Asumimos que las galletas u otros items se modelarían de forma similar o como un producto genérico.
-- Por simplicidad, nos enfocamos en las bebidas que define el LineItem.
INSERT INTO payments (id, order_uuid, last4, card_holder_name, amount, payment_date, payment_method, status)
VALUES (1, 'f47ac10b-58cc-4372-a567-0e02b2c3d479', '1111', 'Ana Perez', 9.50, '2024-05-21', 'CREDIT_CARD', 'COMPLETED');

-- Pedido 2: Un pedido para llevar, pendiente de pago.
INSERT INTO orders (id, uuid, order_date, total_amount, status, location)
VALUES (2, 'd290f1ee-6c54-4b01-90e6-d701748f0851', '2024-05-21 11:00:00', 4.75, 'PAYMENT_EXPECTED', 'TAKE_AWAY');
INSERT INTO order_items (order_id, drink, milk, size, quantity) VALUES (2, 'ESPRESSO', 'SKIMMED', 'SMALL', 1);