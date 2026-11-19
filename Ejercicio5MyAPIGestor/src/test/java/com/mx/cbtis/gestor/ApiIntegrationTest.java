package com.mx.cbtis.gestor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mx.cbtis.gestor.service.EliminacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired EliminacionService eliminacion;
    String call(String path, ObjectNode body) throws Exception {
        return mvc.perform(post(path).contentType("application/json").content(body.toString()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }
    @ParameterizedTest
    @CsvSource({"Usuario,Usuarios,id_usuario,id", "Proyecto,Proyectos,id_proyecto,id_proyecto",
                "Tareas,Tareas,id_tarea,id_tarea", "Comentario,Comentarios,id_comentario,id",
                "Asignacion,Asignaciones,id_asignacion,id"})
    void cicloCrud(String entity, String plural, String idField, String query) throws Exception {
        var payload=mapper.createObjectNode();
        if(entity.equals("Usuario")) { payload.put("nombre","QA"); payload.put("correo_electronico","qa@example.test"); payload.put("contrasena","qa-pass"); }
        if(entity.equals("Proyecto") || entity.equals("Tareas")) payload.put("nombre","QA");
        if(entity.equals("Tareas")) {payload.put("id_proyecto",37); payload.put("lista","pendientes");}
        if(entity.equals("Comentario")) {payload.put("texto_comentario","QA");payload.put("id_tarea",41);}
        if(entity.equals("Asignacion")) {payload.put("id_usuario",43);payload.put("id_tarea",41);}
        assertEquals("1",call("/add"+entity,payload));
        var all=mapper.readTree(call("/buscar"+plural,mapper.createObjectNode()));
        assertEquals(1,all.size());
        int id=all.get(0).get(idField).asInt();payload.put(idField,id);
        String singular=entity.equals("Tareas") ? "Tarea" : entity;
        mvc.perform(get("/buscar"+singular).param(query,""+id)).andExpect(status().isOk()).andExpect(jsonPath("$."+idField).value(id));
        if(entity.equals("Usuario")) {
            var login=mapper.createObjectNode().put("correo_electronico","qa@example.test").put("contrasena","qa-pass");
            assertEquals(id,mapper.readTree(call("/getUsuarioLogin",login)).get("id_usuario").asInt());
            login.put("contrasena","incorrecta");assertEquals("000",mapper.readTree(call("/getUsuarioLogin",login)).get("nombre").asText());
        }
        if(entity.equals("Tareas")) {assertEquals(1,mapper.readTree(call("/getTarea",mapper.createObjectNode().put("id_proyecto",37))).size());assertEquals(0,mapper.readTree(call("/getTarea",mapper.createObjectNode().put("id_proyecto",999))).size());}
        if(entity.equals("Comentario")) {assertEquals(1,mapper.readTree(call("/getComentario",mapper.createObjectNode().put("id_tarea",41))).size());assertEquals(0,mapper.readTree(call("/getComentario",mapper.createObjectNode().put("id_tarea",999))).size());}
        String field=entity.equals("Comentario") ? "texto_comentario" : entity.equals("Asignacion") ? "fecha_asignacion" : "nombre";
        payload.put(field,"editado");assertEquals("1",call("/update"+entity,payload));
        mvc.perform(get("/buscar"+singular).param(query,""+id)).andExpect(jsonPath("$."+field).value("editado"));
        mvc.perform(delete("/delete"+entity).contentType("application/json").content(payload.toString())).andExpect(status().isOk()).andExpect(content().string("1"));
        mvc.perform(get("/buscar"+singular).param(query,""+id)).andExpect(status().isOk()).andExpect(jsonPath("$."+idField).isEmpty());
        assertEquals(0,mapper.readTree(call("/buscar"+plural,mapper.createObjectNode())).size());
    }
    @Test void borradoCoherenteYValidaciones() throws Exception {
        call("/addProyecto",mapper.createObjectNode().put("nombre","Proyecto"));
        int project=mapper.readTree(call("/buscarProyectos",mapper.createObjectNode())).get(0).get("id_proyecto").asInt();
        call("/addTareas",mapper.createObjectNode().put("nombre","Tarea").put("id_proyecto",project));
        int task=mapper.readTree(call("/buscarTareas",mapper.createObjectNode())).get(0).get("id_tarea").asInt();
        call("/addComentario",mapper.createObjectNode().put("texto_comentario","dependiente").put("id_tarea",task));
        call("/addAsignacion",mapper.createObjectNode().put("id_tarea",task));
        eliminacion.eliminarProyecto(project);
        for(String plural:new String[]{"Proyectos","Tareas","Comentarios","Asignaciones"}) assertEquals(0,mapper.readTree(call("/buscar"+plural,mapper.createObjectNode())).size());
        assertThrows(IllegalArgumentException.class,()->eliminacion.eliminarTarea(null));
        assertThrows(IllegalArgumentException.class,()->eliminacion.eliminarProyecto(null));
        eliminacion.eliminarTarea(999);eliminacion.eliminarProyecto(999);
        mvc.perform(post("/addTareas").contentType("application/json").content("{mal" )).andExpect(status().isBadRequest());
    }
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Test void contrasenasProtegidasYCompatibilidad() throws Exception {
        var payload=mapper.createObjectNode().put("nombre","QA secure").put("correo_electronico","secure@example.test").put("contrasena","SoloPruebas2026!");
        call("/addUsuario",payload);
        var json=mapper.readTree(call("/buscarUsuarios",mapper.createObjectNode())).get(0);
        assertFalse(json.has("contrasena"),"La API no debe devolver contrasenas");
        int id=json.get("id_usuario").asInt();
        String hash=jdbc.queryForObject("select contrasena from usuario where id_usuario=?",String.class,id);
        assertTrue(hash.startsWith("$2"));assertNotEquals("SoloPruebas2026!",hash);
        var update=mapper.createObjectNode().put("id_usuario",id).put("nombre","QA updated").put("correo_electronico","secure@example.test");
        call("/updateUsuario",update);
        assertEquals(hash,jdbc.queryForObject("select contrasena from usuario where id_usuario=?",String.class,id));
        call("/addUsuario",update);
        assertEquals(hash,jdbc.queryForObject("select contrasena from usuario where id_usuario=?",String.class,id));
        var login=mapper.createObjectNode().put("correo_electronico","secure@example.test").put("contrasena","SoloPruebas2026!");
        assertEquals(id,mapper.readTree(call("/getUsuarioLogin",login)).get("id_usuario").asInt());
        entityManager.flush(); jdbc.update("update usuario set contrasena=? where id_usuario=?","LegacyPrueba",id); entityManager.clear();
        login.put("contrasena","LegacyPrueba");assertEquals(id,mapper.readTree(call("/getUsuarioLogin",login)).get("id_usuario").asInt());
        login.remove("contrasena");assertEquals("000",mapper.readTree(call("/getUsuarioLogin",login)).get("nombre").asText());
    }
}
