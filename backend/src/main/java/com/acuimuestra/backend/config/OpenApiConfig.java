package com.acuimuestra.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AcuiMuestra API",
                version = "2.0",
                description = "Backend de AcuiMuestra (MVP académico DSY1105, caso ALDEMAR SpA). "
                        + "Datos ficticios. Envíe el encabezado X-Usuario (operador, supervisor o analista). "
                        + "En la nube, además, la clave de API con el botón Authorize."),
        security = @SecurityRequirement(name = "claveApi"))
@SecurityScheme(name = "claveApi", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "X-Api-Key")
public class OpenApiConfig {}
