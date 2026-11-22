package com.acuimuestra.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltroClaveApi extends OncePerRequestFilter {

    static final String ENCABEZADO = "X-Api-Key";

    private final String clave;

    public FiltroClaveApi(@Value("${acuimuestra.api-key:}") String clave) {
        this.clave = clave;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return clave.isBlank() || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (clave.equals(request.getHeader(ENCABEZADO))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"Falta o es incorrecta la clave de API (X-Api-Key)\"}");
    }
}
