package com.mx.cbtis.gestor.persistence;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;
@Converter
public class FechaConverter implements AttributeConverter<String, LocalDate> {
    public LocalDate convertToDatabaseColumn(String value) { return value == null || value.isBlank() ? null : LocalDate.parse(value); }
    public String convertToEntityAttribute(LocalDate value) { return value == null ? null : value.toString(); }
}
