package com.mx.cbtis.gestor;
import com.mx.cbtis.gestor.modelo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ModelosContractTest {
    final ObjectMapper mapper = new ObjectMapper();
    @Test void contratoAsignaciones() throws Exception {
        var value = new Asignaciones(17, 17, 17, "dato_fecha_asignacion");
        assertEquals(17, value.getId_asignacion());
        assertEquals(17, value.getId_usuario());
        assertEquals(17, value.getId_tarea());
        assertEquals("dato_fecha_asignacion", value.getFecha_asignacion());
        var blank = new Asignaciones();
        blank.setId_asignacion(17);
        blank.setId_usuario(17);
        blank.setId_tarea(17);
        blank.setFecha_asignacion("dato_fecha_asignacion");
        assertEquals(mapper.valueToTree(value), mapper.valueToTree(blank));
        assertEquals(mapper.valueToTree(value), mapper.valueToTree(mapper.readValue(mapper.writeValueAsString(value), Asignaciones.class)));
    }
    @Test void contratoComentarios() throws Exception {
        var value = new Comentarios(17, "dato_id_usuario", 17, "dato_texto_comentario", "dato_fecha_hora");
        assertEquals(17, value.getId_comentario());
        assertEquals("dato_id_usuario", value.getId_usuario());
        assertEquals(17, value.getId_tarea());
        assertEquals("dato_texto_comentario", value.getTexto_comentario());
        assertEquals("dato_fecha_hora", value.getFecha_hora());
        var blank = new Comentarios();
        blank.setId_comentario(17);
        blank.setId_usuario("dato_id_usuario");
        blank.setId_tarea(17);
        blank.setTexto_comentario("dato_texto_comentario");
        blank.setFecha_hora("dato_fecha_hora");
        assertEquals(mapper.valueToTree(value), mapper.valueToTree(blank));
        assertEquals(mapper.valueToTree(value), mapper.valueToTree(mapper.readValue(mapper.writeValueAsString(value), Comentarios.class)));
    }
    @Test void contratoProyectos() throws Exception {
        var value = new Proyectos(17, "dato_nombre", "dato_descripcion", "dato_fecha_inicio", "dato_fecha_finalizacion", "dato_estado_proyecto", "dato_fondo");
        assertEquals(17, value.getId_proyecto());
        assertEquals("dato_nombre", value.getNombre());
        assertEquals("dato_descripcion", value.getDescripcion());
        assertEquals("dato_fecha_inicio", value.getFecha_inicio());
        assertEquals("dato_fecha_finalizacion", value.getFecha_finalizacion());
        assertEquals("dato_estado_proyecto", value.getEstado_proyecto());
        assertEquals("dato_fondo", value.getFondo());
        var blank = new Proyectos();
        blank.setId_proyecto(17);
        blank.setNombre("dato_nombre");
        blank.setDescripcion("dato_descripcion");
        blank.setFecha_inicio("dato_fecha_inicio");
        blank.setFecha_finalizacion("dato_fecha_finalizacion");
        blank.setEstado_proyecto("dato_estado_proyecto");
        blank.setFondo("dato_fondo");
        assertEquals(mapper.valueToTree(value), mapper.valueToTree(blank));
        assertEquals(mapper.valueToTree(value), mapper.valueToTree(mapper.readValue(mapper.writeValueAsString(value), Proyectos.class)));
    }
    @Test void contratoTareas() throws Exception {
        var value = new Tareas(17, "dato_nombre", "dato_descripcion", "dato_fecha_vencimiento", "dato_lista", "dato_estado", 17);
        assertEquals(17, value.getId_tarea());
        assertEquals("dato_nombre", value.getNombre());
        assertEquals("dato_descripcion", value.getDescripcion());
        assertEquals("dato_fecha_vencimiento", value.getFecha_vencimiento());
        assertEquals("dato_lista", value.getLista());
        assertEquals("dato_estado", value.getEstado());
        assertEquals(17, value.getId_proyecto());
        var blank = new Tareas();
        blank.setId_tarea(17);
        blank.setNombre("dato_nombre");
        blank.setDescripcion("dato_descripcion");
        blank.setFecha_vencimiento("dato_fecha_vencimiento");
        blank.setLista("dato_lista");
        blank.setEstado("dato_estado");
        blank.setId_proyecto(17);
        assertEquals(mapper.valueToTree(value), mapper.valueToTree(blank));
        assertEquals(mapper.valueToTree(value), mapper.valueToTree(mapper.readValue(mapper.writeValueAsString(value), Tareas.class)));
    }
    @Test void contratoUsuario() throws Exception {
        var value = new Usuario(17, "dato_nombre", "dato_correo_electronico", "dato_contrasena", "dato_rol", "dato_imagen_usuario");
        assertEquals(17, value.getId_usuario());
        assertEquals("dato_nombre", value.getNombre());
        assertEquals("dato_correo_electronico", value.getCorreo_electronico());
        assertEquals("dato_contrasena", value.getContrasena());
        assertEquals("dato_rol", value.getRol());
        assertEquals("dato_imagen_usuario", value.getImagen_usuario());
        var blank = new Usuario();
        blank.setId_usuario(17);
        blank.setNombre("dato_nombre");
        blank.setCorreo_electronico("dato_correo_electronico");
        blank.setContrasena("dato_contrasena");
        blank.setRol("dato_rol");
        blank.setImagen_usuario("dato_imagen_usuario");
        assertEquals(mapper.valueToTree(value), mapper.valueToTree(blank));
        assertEquals(mapper.valueToTree(value), mapper.valueToTree(mapper.readValue(mapper.writeValueAsString(value), Usuario.class)));
    }
}
