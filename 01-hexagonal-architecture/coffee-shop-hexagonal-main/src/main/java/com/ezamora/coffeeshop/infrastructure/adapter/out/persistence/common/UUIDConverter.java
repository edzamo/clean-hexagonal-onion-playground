package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

/** Convierte UUID <-> VARCHAR(36): MySQL no tiene tipo UUID nativo. */
@Converter(autoApply = true)
public class UUIDConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        return dbData != null ? UUID.fromString(dbData) : null;
    }
}
