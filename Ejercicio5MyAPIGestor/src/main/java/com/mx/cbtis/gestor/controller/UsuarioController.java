package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@RestController
public class UsuarioController {
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    private boolean coincide(String provided, String stored) {
        if (provided == null || stored == null) return false;
        if (stored.startsWith("$2")) return encoder.matches(provided, stored);
        // Compatibility with existing classroom accounts. New and edited passwords are always hashed.
        return java.security.MessageDigest.isEqual(provided.getBytes(java.nio.charset.StandardCharsets.UTF_8), stored.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }


	@Autowired
	UsuarioRepository usuariotable;

	@PostMapping("/getUsuarioLogin")
	public Usuario getUsuarioLogin(@RequestBody Usuario userParam) {

        Optional<Usuario> optionaluser = usuariotable.findByCorreo(userParam.getCorreo_electronico());
        optionaluser = optionaluser.filter(u -> coincide(userParam.getContrasena(), u.getContrasena()));

		Usuario user = new Usuario();

		if(optionaluser.isPresent()) {
			user = optionaluser.get();
		}else {
			user.setNombre("000");
		}

		return user;
	}

	@GetMapping("/buscarUsuario")
	public Usuario getUsuario(@RequestParam int id) {

		Usuario buscar = new Usuario();
		Optional<Usuario> usuario = usuariotable.findById(id);
		if(usuario.isPresent())
		{
			buscar = usuario.get();
		}

		return buscar;
	}

	@PostMapping("/addUsuario")
	public int addUsuario(@RequestBody Usuario usuario) {
		int flag = 0;
		if(usuario != null) {
			if (usuario.getContrasena() == null && usuario.getId_usuario() != null) {
                usuariotable.findById(usuario.getId_usuario()).ifPresent(existing -> usuario.setContrasena(existing.getContrasena()));
            }
            if (usuario.getContrasena() != null && !usuario.getContrasena().startsWith("$2")) {
                usuario.setContrasena(encoder.encode(usuario.getContrasena()));
            }
            usuariotable.save(usuario);
			flag = 1;
		}

		return flag;
	}

	@PostMapping("/updateUsuario")
	public int updateUsuario(@RequestBody Usuario usuario) {
		int flag = 0;
		if(usuario != null) {
			if (usuario.getContrasena() == null && usuario.getId_usuario() != null) {
                usuariotable.findById(usuario.getId_usuario()).ifPresent(existing -> usuario.setContrasena(existing.getContrasena()));
            }
            if (usuario.getContrasena() != null && !usuario.getContrasena().startsWith("$2")) {
                usuario.setContrasena(encoder.encode(usuario.getContrasena()));
            }
            usuariotable.save(usuario);
			flag = 1;
		}

		return flag;
	}

	@DeleteMapping("/deleteUsuario")
	public int deleteUsuario(@RequestBody Usuario usuario) {
		int flag = 0;
		if(usuario != null) {
			usuariotable.delete(usuario);
			flag = 1;
		}

		return flag;
	}

	@PostMapping("/buscarUsuarios")
	public List<Usuario> getUsuarios(){
		List<Usuario> listaUsuarios = new ArrayList<>();
		usuariotable.findAll().forEach(listaUsuarios::add);
		return listaUsuarios;
	}
}
