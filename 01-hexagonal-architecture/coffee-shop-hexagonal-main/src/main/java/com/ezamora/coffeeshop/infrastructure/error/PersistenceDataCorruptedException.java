package com.ezamora.coffeeshop.infrastructure.error;

/**
 * Los datos almacenados violan invariantes del dominio (fila corrupta). Es un fallo del servidor,
 * no del cliente: se traduce a 500 genérico. Vive fuera de adapter.in/out para poder compartirse.
 */
public class PersistenceDataCorruptedException extends RuntimeException {

    public PersistenceDataCorruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
