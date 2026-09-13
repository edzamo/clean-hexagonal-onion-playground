CREATE TABLE IF NOT EXISTS "appointments" (
    "id" UUID PRIMARY KEY,
    "patient_id" UUID NOT NULL,
    "practitioner_id" UUID NOT NULL,
    "start_time" TIMESTAMP NOT NULL,
    "end_time" TIMESTAMP NOT NULL,
    "reason" VARCHAR(500),
    "status" VARCHAR(20) NOT NULL,
    "cancellation_description" VARCHAR(500),
    "cancelled_at" TIMESTAMP
);
