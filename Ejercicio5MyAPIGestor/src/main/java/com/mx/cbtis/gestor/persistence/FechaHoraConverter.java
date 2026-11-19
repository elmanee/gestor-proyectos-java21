package com.mx.cbtis.gestor.persistence;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Converter
public class FechaHoraConverter implements AttributeConverter<String, LocalDateTime> {
    public LocalDateTime convertToDatabaseColumn(String value) { return value == null || value.isBlank() ? null : LocalDateTime.parse(value.replace(' ', 'T')); }
    public String convertToEntityAttribute(LocalDateTime value) { return value == null ? null : value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
}
