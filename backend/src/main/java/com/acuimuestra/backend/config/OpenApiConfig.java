package com.acuimuestra.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "AcuiMuestra API",
        version = "2.0",
        description = "Backend de AcuiMuestra (MVP académico DSY1105, caso ALDEMAR SpA). "
                + "Datos ficticios. Envíe el encabezado X-Usuario (operador, supervisor o analista)."))
public class OpenApiConfig {}
