package com.mx.cbtis.gestor.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mx.cbtis.gestor.modelo.Asignaciones;

public interface AsignacionesRepository extends CrudRepository<Asignaciones,Integer>{
	@Modifying
	@Query(value = "delete from asignaciones where id_tarea = :id_tarea", nativeQuery = true)
	void eliminarPorTarea(@Param("id_tarea") Integer idTarea);

}
