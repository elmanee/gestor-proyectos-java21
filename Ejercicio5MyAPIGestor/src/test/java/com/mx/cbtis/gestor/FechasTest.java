package com.mx.cbtis.gestor;
import com.mx.cbtis.gestor.persistence.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class FechasTest {
    @Test void fechaRespetaContratoYValoresVacios() {
        var c=new FechaConverter();assertNull(c.convertToDatabaseColumn(null));assertNull(c.convertToDatabaseColumn(" "));
        assertNull(c.convertToEntityAttribute(null));assertEquals(LocalDate.of(2026,11,19),c.convertToDatabaseColumn("2026-11-19"));
        assertEquals("2026-11-19",c.convertToEntityAttribute(LocalDate.of(2026,11,19)));
        assertThrows(java.time.format.DateTimeParseException.class,()->c.convertToDatabaseColumn("fecha-invalida"));
    }
    @Test void fechaHoraRespetaFormatoSqlEIso() {
        var c=new FechaHoraConverter();assertNull(c.convertToDatabaseColumn(null));assertNull(c.convertToDatabaseColumn(" "));assertNull(c.convertToEntityAttribute(null));
        var expected=LocalDateTime.of(2026,11,19,10,0);
        assertEquals(expected,c.convertToDatabaseColumn("2026-11-19 10:00:00"));assertEquals(expected,c.convertToDatabaseColumn("2026-11-19T10:00:00"));
        assertEquals("2026-11-19 10:00:00",c.convertToEntityAttribute(expected));
        assertThrows(java.time.format.DateTimeParseException.class,()->c.convertToDatabaseColumn("incorrecto"));
    }
}
