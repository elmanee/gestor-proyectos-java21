package com.mx.cbtis.gestor.service;

import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.ProtectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminacionService {
    private final ProtectosRepository proyectos;
    private final TareasRepository tareas;
    private final ComentariosRepository comentarios;
    private final AsignacionesRepository asignaciones;

    public EliminacionService(ProtectosRepository proyectos, TareasRepository tareas,
                              ComentariosRepository comentarios, AsignacionesRepository asignaciones) {
        this.proyectos = proyectos;
        this.tareas = tareas;
        this.comentarios = comentarios;
        this.asignaciones = asignaciones;
    }

    @Transactional
    public void eliminarTarea(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Se requiere el identificador de la tarea");
        }
        comentarios.eliminarPorTarea(id);
        asignaciones.eliminarPorTarea(id);
        tareas.findById(id).ifPresent(tareas::delete);
    }

    @Transactional
    public void eliminarProyecto(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Se requiere el identificador del proyecto");
        }
        for (Tareas tarea : tareas.findByIdProyecto(id)) {
            eliminarTarea(tarea.getId_tarea());
        }
        proyectos.findById(id).ifPresent(proyectos::delete);
    }
}
