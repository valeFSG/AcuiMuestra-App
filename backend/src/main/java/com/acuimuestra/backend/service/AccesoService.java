package com.acuimuestra.backend.service;

import com.acuimuestra.backend.model.Rol;
import com.acuimuestra.backend.model.Usuario;
import com.acuimuestra.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AccesoService {

    private final UsuarioRepository usuarios;

    public AccesoService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario requerir(String nombreUsuario, Rol... permitidos) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Falta el encabezado X-Usuario");
        }
        Usuario usuario = usuarios.findByNombreUsuario(nombreUsuario.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no registrado"));
        if (permitidos.length > 0 && Arrays.stream(permitidos).noneMatch(r -> r == usuario.getRol())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "El rol " + usuario.getRol() + " no puede realizar esta acción");
        }
        return usuario;
    }
}
