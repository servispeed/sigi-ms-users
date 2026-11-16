package com.imprenta.personas.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.imprenta.personas.dto.UsuarioDTO;
import com.imprenta.personas.exception.*;
import com.imprenta.personas.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.imprenta.personas.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Integer ROL_DISENADOR = 5;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioDTO> findAllDTO() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        return usuarios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID" + id + " no encontrado"));

    }

    public Usuario save(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }
        String passwordPlano = usuario.getPassword();
        String passwordHasheado = passwordEncoder.encode(passwordPlano);
        usuario.setPassword(passwordHasheado);

        return usuarioRepository.save(usuario);
    }

    public Usuario update(Integer id, Usuario datosActualizados) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));

        if (!usuarioExistente.getEmail().equals(datosActualizados.getEmail()) &&
                usuarioRepository.existsByEmail(datosActualizados.getEmail())) {
            throw new BadRequestException("El email ya está en uso por otro usuario");
        }

        usuarioExistente.setNombre(datosActualizados.getNombre());
        usuarioExistente.setEmail(datosActualizados.getEmail());

        String passwordNuevo = datosActualizados.getPassword();
        if (passwordNuevo != null && !passwordNuevo.isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(passwordNuevo));

        }
        usuarioExistente.setRol(datosActualizados.getRol());

        return usuarioRepository.save(usuarioExistente);
    }

    public void delete(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar, usuario con ID " + id + " no existe");
        }
        usuarioRepository.deleteById(id);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<UsuarioDTO> obtenerListaDeDisenadores() {
        List<Usuario> usuarios = usuarioRepository.findByIdRol(ROL_DISENADOR);

        return usuarios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private UsuarioDTO convertirADTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();

        dto.setIdUsuario(u.getIdUser());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setIdRol(u.getRol());

        return dto;
    }

    @Transactional
    public void cambiarContrasena(Integer idUsuario, String passwordActual, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + idUsuario + " no encontrado"));

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }
        String hashNuevaPassword = passwordEncoder.encode(nuevaPassword);

        usuario.setPassword(hashNuevaPassword);
        usuarioRepository.save(usuario);
    }
}
