package com.mx.cbtis;
import com.mx.cbtis.controller.GestorController;
import com.mx.cbtis.modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class GestorControllerTest {
    GestorController controller;
    MockRestServiceServer server;
    ExtendedModelMap model;
    @BeforeEach void setup() {
        RestTemplate template=new RestTemplate();
        RestTemplateBuilder builder=mock(RestTemplateBuilder.class);
        when(builder.build()).thenReturn(template);
        controller=new GestorController(builder,"http://api.test");
        server=MockRestServiceServer.bindTo(template).build();
        model=new ExtendedModelMap();
    }
    void expect(String path, HttpMethod method, String json) {
        server.expect(requestTo("http://api.test"+path)).andExpect(method(method))
            .andRespond(withSuccess(json,MediaType.APPLICATION_JSON));
    }
    @Test void vistasYModelos() {
        assertEquals("home",controller.getHome());assertEquals("tutorial",controller.getTutorial());
        assertEquals("manual",controller.getManual());assertEquals("login",controller.getLogin());
        assertEquals("register",controller.getRegister(model));assertInstanceOf(Usuario.class,model.get("usuario"));
        assertEquals("inicio",controller.getInicio(model));assertInstanceOf(Proyectos.class,model.get("proyectos"));
        assertEquals("redirect:/inicio",controller.getTarea(null));assertEquals("redirect:/proyecto?id_proyecto=17",controller.getTarea(17));
    }
    @Test void registroYProyecto() {
        expect("/addUsuario",HttpMethod.POST,"1");
        assertEquals("redirect:/login",controller.setSaveUsuario(new Usuario()).getViewName());server.verify();
    }
    @Test void guardarProyecto() {
        expect("/addProyecto",HttpMethod.POST,"1");
        assertEquals("redirect:/inicio",controller.setSaveProyecto(new Proyectos()).getViewName());server.verify();
    }
    @Test void consultarProyectos() {
        expect("/buscarProyectos",HttpMethod.POST,"[{\"id_proyecto\":17,\"nombre\":\"QA\"}]");
        var result=controller.consultarProyectos(model);
        assertEquals(200,result.getStatusCode().value());
        assertEquals("QA",((java.util.List<Proyectos>)result.getBody()).get(0).getNombre());
        assertEquals(result.getBody(),model.get("listaProyectos"));server.verify();
    }
    @Test void eliminarProyecto() {
        expect("/deleteProyecto",HttpMethod.DELETE,"1");
        var p=new Proyectos();p.setId_proyecto(17);
        assertEquals("redirect:/inicio",controller.eliminarProyecto(p).getViewName());server.verify();
    }
    @Test void cargarProyecto() {
        expect("/buscarProyecto?id_proyecto=17",HttpMethod.GET,"{\"id_proyecto\":17,\"nombre\":\"QA\"}");
        var p=new Proyectos();p.setId_proyecto(17);
        assertEquals("QA",((Proyectos)controller.updateProyecto(p).getBody()).getNombre());server.verify();
    }
    @Test void login() {
        expect("/getUsuarioLogin",HttpMethod.POST,"{\"id_usuario\":17,\"nombre\":\"QA\"}");
        assertEquals(17,controller.verificarUsuario(new Usuario()).getBody().getId_usuario());server.verify();
    }
    @Test void usuarios() {
        expect("/buscarUsuarios",HttpMethod.POST,"[{\"id_usuario\":17}]");
        assertEquals(1,((java.util.List<?>)controller.consultarUsuarios(model).getBody()).size());
        assertTrue(model.containsKey("listaUsuarios"));server.verify();
    }
    @Test void tareasDelProyecto() {
        expect("/getTarea",HttpMethod.POST,"[{\"id_tarea\":18,\"id_proyecto\":17}]");
        var task=new Tareas();task.setId_proyecto(17);
        assertEquals(18,controller.obtenerTareas(task,model).getBody().get(0).getId_tarea());server.verify();
    }
    @Test void comentariosDeTarea() {
        expect("/getComentario",HttpMethod.POST,"[{\"texto_comentario\":\"QA\"}]");
        assertEquals("QA",controller.obtenerComentarios(new Comentarios(),model).getBody().get(0).getTexto_comentario());server.verify();
    }
    @Test void tablero() {
        expect("/buscarUsuarios",HttpMethod.POST,"[]");
        assertEquals("proyecto",controller.getProyecto(model));
        assertInstanceOf(Tareas.class,model.get("tarea"));assertInstanceOf(Comentarios.class,model.get("comentarios"));
        assertEquals(java.util.List.of(),model.get("listaUsuarios"));server.verify();
    }
    @Test void guardarTarea() {
        expect("/addTareas",HttpMethod.POST,"1");var t=new Tareas();t.setId_proyecto(17);
        var result=controller.setSaveTareas(t);
        assertEquals("redirect:/proyecto",result.getViewName());assertEquals(17,result.getModel().get("id_proyecto"));server.verify();
    }
    @Test void comentarioValidoEInvalido() {
        var c=new Comentarios();assertEquals(400,controller.setSaveComentario(c).getStatusCode().value());
        c.setTexto_comentario(" ");assertEquals(400,controller.setSaveComentario(c).getStatusCode().value());
        c.setTexto_comentario("QA");expect("/addComentario",HttpMethod.POST,"1");
        assertEquals(1,controller.setSaveComentario(c).getBody());server.verify();
    }
    @Test void cargarTarea() {
        expect("/buscarTarea?id_tarea=18",HttpMethod.GET,"{\"id_tarea\":18,\"nombre\":\"QA\"}");
        var task=new Tareas();task.setId_tarea(18);
        assertEquals("QA",((Tareas)controller.updateTarea(task).getBody()).getNombre());server.verify();
    }
    @Test void eliminarTarea() {
        expect("/deleteTareas",HttpMethod.DELETE,"1");var t=new Tareas();t.setId_tarea(18);
        assertEquals("redirect:/proyecto",controller.eliminarTarea(t).getViewName());server.verify();
    }
    @Test void crearTarea() {
        expect("/addTareas",HttpMethod.POST,"1");assertEquals(1,controller.verificarTarea(new Tareas()).getBody());server.verify();
    }
    @Test void cambiarTarea() {
        expect("/addTareas",HttpMethod.POST,"1");assertEquals(1,controller.cambiosTareas(new Tareas()).getBody());server.verify();
    }
    @Test void consultarTodasTareas() {
        expect("/buscarTareas",HttpMethod.POST,"[]");assertEquals(java.util.List.of(),controller.consultarTareas().getBody());server.verify();
    }
    @Test void consultarTodosComentarios() {
        expect("/buscarComentarios",HttpMethod.POST,"[]");assertEquals(java.util.List.of(),controller.consultarComentarios().getBody());server.verify();
    }
    @Test void errorDeApiNoSeOculta() {
        server.expect(requestTo("http://api.test/buscarTareas")).andRespond(withServerError());
        assertThrows(HttpServerErrorException.class,()->controller.consultarTareas());server.verify();
    }
}
