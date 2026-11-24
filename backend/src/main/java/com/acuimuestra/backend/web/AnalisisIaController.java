package com.acuimuestra.backend.web;

import com.acuimuestra.backend.dto.AnalisisIaResponse;
import com.acuimuestra.backend.service.AnalisisIaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/muestras")
@Tag(name = "Análisis con IA", description = "Segunda opinión sobre la foto de una muestra con un modelo de visión")
public class AnalisisIaController {

    private final AnalisisIaService servicio;

    public AnalisisIaController(AnalisisIaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/{id}/analisis-ia")
    @Operation(summary = "Analiza la foto con IA (supervisor o analista): conteo estimado, cholgas y calidad de la foto")
    public AnalisisIaResponse analizar(@RequestHeader("X-Usuario") String usuario, @PathVariable String id) {
        return servicio.analizar(usuario, id);
    }
}
