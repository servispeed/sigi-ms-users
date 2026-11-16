package com.imprenta.personas.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imprenta.personas.dto.CambiarPasswordDTO;
import com.imprenta.personas.dto.UsuarioDTO;
import com.imprenta.personas.model.Usuario;
import com.imprenta.personas.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAll() {
        List<UsuarioDTO> usuarios = usuarioService.findAllDTO();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getById(@PathVariable Integer id) {
        Usuario usuario = usuarioService.findById(id);

        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUser());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setIdRol(usuario.getRol());

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Usuario> create(@RequestBody Usuario usuario) {
        Usuario creado = usuarioService.save(usuario);
        return ResponseEntity.status(201).body(creado);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable Integer id, @RequestBody Usuario usuario) {
        Usuario actualizado = usuarioService.update(id, usuario);

        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            usuarioService.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> findByEmail(@PathVariable String email) {
        return usuarioService.findByEmail(email)
                .map(usuario -> ResponseEntity.ok(usuario))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/disenadores")
    public ResponseEntity<List<UsuarioDTO>> getDisenadores() {
        return ResponseEntity.ok(usuarioService.obtenerListaDeDisenadores());
    }

    @PutMapping("/{idUsuario}/cambiar-password")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @PathVariable Integer idUsuario,
            @RequestBody CambiarPasswordDTO cambiarPasswordDTO) {
        usuarioService.cambiarContrasena(idUsuario,
                cambiarPasswordDTO.getPasswordActual(),
                cambiarPasswordDTO.getNuevaPassword());

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Contraseña actualizada exitosamente");
        return ResponseEntity.ok(response);
    }

}
