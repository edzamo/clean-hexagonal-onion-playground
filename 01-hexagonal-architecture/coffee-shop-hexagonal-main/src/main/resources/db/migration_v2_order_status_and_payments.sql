-- =================================================================
-- Migración v2 (MySQL) para bases de datos existentes creadas con el esquema anterior.
-- Ejecutar UNA vez, antes de arrancar la nueva versión (ddl-auto: update no migra datos).
--
-- 1) Estados de orden: OrderStatus persistido pasa a los 5 estados del dominio.
--    PENDING_PAYMENT -> PAYMENT_EXPECTED
--    COMPLETED       -> PAID   (el estado real posterior, PREPARING/READY/TAKEN, no se conservaba;
--                               las filas COMPLETED antiguas se interpretan como pagadas)
--    CANCELLED       -> se elimina: cancelar borra la orden. Se borran las filas CANCELLED restantes.
-- 2) Pagos: nueva forma (order_uuid, last4, card_holder_name) y payment_date DATE.
--    Los pagos antiguos no tenían titular ni last4; se rellenan con marcadores.
-- 3) Bloqueo optimista: columna orders.version (OrderJpaEntity.@Version).
-- =================================================================

ALTER TABLE orders ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

UPDATE orders SET status = 'PAYMENT_EXPECTED' WHERE status = 'PENDING_PAYMENT';
UPDATE orders SET status = 'PAID' WHERE status = 'COMPLETED';

DELETE FROM payments WHERE order_id IN (SELECT id FROM orders WHERE status = 'CANCELLED');
DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE status = 'CANCELLED');
DELETE FROM orders WHERE status = 'CANCELLED';

ALTER TABLE payments
    ADD COLUMN order_uuid VARCHAR(36) NULL,
    ADD COLUMN last4 VARCHAR(4) NOT NULL DEFAULT '0000',
    ADD COLUMN card_holder_name VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN',
    MODIFY COLUMN payment_date DATE NOT NULL;

UPDATE payments p JOIN orders o ON o.id = p.order_id SET p.order_uuid = o.uuid;

ALTER TABLE payments
    MODIFY COLUMN order_uuid VARCHAR(36) NOT NULL,
    ADD CONSTRAINT uq_payments_order_uuid UNIQUE (order_uuid),
    ADD CONSTRAINT fk_payments_order_uuid FOREIGN KEY (order_uuid) REFERENCES orders(uuid),
    DROP FOREIGN KEY payments_ibfk_1,
    DROP COLUMN order_id,
    DROP COLUMN uuid;
