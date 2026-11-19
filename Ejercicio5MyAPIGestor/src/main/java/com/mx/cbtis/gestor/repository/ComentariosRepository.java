package com.mx.cbtis.gestor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.mx.cbtis.gestor.modelo.Comentarios;

public interface ComentariosRepository extends CrudRepository<Comentarios,Integer>{
	@Modifying
	@Query(value = "delete from comentarios where id_tarea = :id_tarea", nativeQuery = true)
	void eliminarPorTarea(@Param("id_tarea") Integer idTarea);

	@Query(value = "select id_comentario, id_tarea, texto_comentario, fecha_hora, id_usuario "
			+ "from comentarios c where id_tarea = :id_tarea", nativeQuery = true)
	List<Comentarios> findByTarea(@Param("id_tarea") Integer id_tarea);

}
