package com.acuimuestra.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
public class AnalizadorConRespaldo implements AnalizadorIa {

    private static final Logger log = LoggerFactory.getLogger(AnalizadorConRespaldo.class);

    private final List<AnalizadorIa> proveedores;

    public AnalizadorConRespaldo(GeminiAnalizador gemini, GroqAnalizador groq) {
        this.proveedores = List.of(gemini, groq);
    }

    @Override
    public boolean configurado() {
        return proveedores.stream().anyMatch(AnalizadorIa::configurado);
    }

    @Override
    public String modelo() {
        return proveedores.stream().filter(AnalizadorIa::configurado).findFirst()
                .map(AnalizadorIa::modelo).orElse("sin configurar");
    }

    @Override
    public ResultadoIa analizar(byte[] fotoJpeg) {
        ApiException ultimo = null;
        for (AnalizadorIa proveedor : proveedores) {
            if (!proveedor.configurado()) continue;
            try {
                return proveedor.analizar(fotoJpeg);
            } catch (ApiException e) {
                ultimo = e;
                log.warn("{} no pudo analizar la foto: {}", proveedor.getClass().getSimpleName(), e.getMessage());
            }
        }
        throw ultimo != null ? ultimo
                : new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El análisis con IA no está configurado en el servidor");
    }
}
