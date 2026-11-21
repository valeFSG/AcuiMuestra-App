package com.acuimuestra.backend.web;

import com.acuimuestra.backend.model.Usuario;
import com.acuimuestra.backend.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Usuarios ficticios y sus roles")
public class UsuarioController {

    private final UsuarioRepository usuarios;

    public UsuarioController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    @Operation(summary = "Lista los usuarios ficticios con su rol")
    public List<Usuario> listar() {
        return usuarios.findAll();
    }
}
