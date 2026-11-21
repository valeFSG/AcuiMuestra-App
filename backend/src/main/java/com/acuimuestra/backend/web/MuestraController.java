package com.acuimuestra.backend.web;

import com.acuimuestra.backend.dto.CambioEstadoRequest;
import com.acuimuestra.backend.dto.MuestraRequest;
import com.acuimuestra.backend.model.EstadoRevision;
import com.acuimuestra.backend.model.Muestra;
import com.acuimuestra.backend.service.MuestraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/muestras")
@Tag(name = "Muestras", description = "Registro, corrección y revisión de muestras de choritos")
public class MuestraController {

    private static final String USUARIO = "X-Usuario";

    private final MuestraService servicio;

    public MuestraController(MuestraService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista las muestras, con filtros opcionales por centro y estado")
    public List<Muestra> listar(@RequestHeader(USUARIO) String usuario,
                                @RequestParam(required = false) String centro,
                                @RequestParam(required = false) EstadoRevision estado) {
        return servicio.listar(usuario, centro, estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una muestra por su id")
    public Muestra obtener(@RequestHeader(USUARIO) String usuario, @PathVariable String id) {
        return servicio.obtener(usuario, id);
    }

    @PostMapping
    @Operation(summary = "Registra una muestra (operador). Si el id ya existe, la devuelve sin duplicarla")
    public ResponseEntity<Muestra> crear(@RequestHeader(USUARIO) String usuario,
                                         @Valid @RequestBody MuestraRequest req) {
        MuestraService.Creacion r = servicio.crear(usuario, req);
        return ResponseEntity.status(r.nueva() ? HttpStatus.CREATED : HttpStatus.OK).body(r.muestra());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Corrige una muestra observada (operador)")
    public Muestra corregir(@RequestHeader(USUARIO) String usuario, @PathVariable String id,
                            @Valid @RequestBody MuestraRequest req) {
        return servicio.corregir(usuario, id, req);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Observa, valida o descarta una muestra (supervisor)")
    public Muestra cambiarEstado(@RequestHeader(USUARIO) String usuario, @PathVariable String id,
                                 @Valid @RequestBody CambioEstadoRequest req) {
        return servicio.cambiarEstado(usuario, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una muestra (solo supervisor; en la app se usa Descartar para mantener la trazabilidad)")
    public void eliminar(@RequestHeader(USUARIO) String usuario, @PathVariable String id) {
        servicio.eliminar(usuario, id);
    }
}
