package com.acuimuestra.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltroClaveApi extends OncePerRequestFilter {

    static final String ENCABEZADO = "X-Api-Key";
    private static final Logger log = LoggerFactory.getLogger(FiltroClaveApi.class);

    private final String clave;

    public FiltroClaveApi(@Value("${acuimuestra.api-key:}") String clave) {
        this.clave = limpiar(clave);
        log.info(this.clave.isEmpty()
                ? "Clave de API desactivada"
                : "Clave de API activa (" + this.clave.length() + " caracteres)");
    }

    static String limpiar(String valor) {
        return valor == null ? "" : valor.strip();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return clave.isEmpty() || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recibida = limpiar(request.getHeader(ENCABEZADO));
        if (clave.equals(recibida)) {
            chain.doFilter(request, response);
            return;
        }
        log.warn("Clave de API rechazada: recibida de {} caracteres, esperada de {}", recibida.length(), clave.length());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"Falta o es incorrecta la clave de API (X-Api-Key)\"}");
    }
}
