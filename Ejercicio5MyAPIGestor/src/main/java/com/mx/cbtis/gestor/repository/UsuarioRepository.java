package com.mx.cbtis.gestor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.mx.cbtis.gestor.modelo.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario,Integer> {

    @Query(value = "select * from usuario where correo_electronico = :correo_electronico", nativeQuery = true)
    Optional<Usuario> findByCorreo(@Param("correo_electronico") String correo);
}
