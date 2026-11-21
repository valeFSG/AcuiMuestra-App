package com.acuimuestra.backend.web;

import com.acuimuestra.backend.dto.ResumenResponse;
import com.acuimuestra.backend.service.MuestraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Indicadores para el dashboard")
public class ReporteController {

    private final MuestraService servicio;

    public ReporteController(MuestraService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Total de muestras, individuos contados y cantidad por estado y por centro")
    public ResumenResponse resumen(@RequestHeader("X-Usuario") String usuario) {
        return servicio.resumen(usuario);
    }
}
