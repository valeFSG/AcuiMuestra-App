package com.acuimuestra.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class GroqAnalizador implements AnalizadorIa {

    private static final String URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final Logger log = LoggerFactory.getLogger(GroqAnalizador.class);

    private final RestClient http;
    private final String clave;
    private final String modelo;

    public GroqAnalizador(@Value("${acuimuestra.groq.api-key:}") String clave,
                          @Value("${acuimuestra.groq.modelo:qwen/qwen3.8-27b}") String modelo) {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory();
        fabrica.setReadTimeout(Duration.ofSeconds(20));
        this.http = RestClient.builder().requestFactory(fabrica).build();
        this.clave = clave == null ? "" : clave.strip();
        this.modelo = modelo == null || modelo.isBlank() ? "qwen/qwen3.8-27b" : modelo.strip();
    }

    @Override
    public boolean configurado() {
        return !clave.isEmpty();
    }

    @Override
    public String modelo() {
        return modelo;
    }

    @Override
    public ResultadoIa analizar(byte[] fotoJpeg) {
        String imagen = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(fotoJpeg);
        Map<String, Object> cuerpo = Map.of(
                "model", modelo,
                "temperature", 0,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", RespuestaIa.INSTRUCCION),
                                Map.of("type", "image_url", "image_url", Map.of("url", imagen))))));
        try {
            Map<?, ?> respuesta = http.post()
                    .uri(URL)
                    .header("Authorization", "Bearer " + clave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(Map.class);
            return RespuestaIa.interpretar(textoDe(respuesta)).conModelo(modelo);
        } catch (RestClientResponseException e) {
            int codigo = e.getStatusCode().value();
            String detalle = e.getResponseBodyAsString();
            log.warn("Groq ({}) respondió {}: {}", modelo, codigo,
                    detalle.length() > 300 ? detalle.substring(0, 300) + "…" : detalle);
            throw switch (codigo) {
                case 429 -> new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Se alcanzó el límite gratuito de la IA. Intenta en unos minutos.");
                case 503 -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "La IA está ocupada. Intenta en un momento.");
                case 401, 403 -> new ApiException(HttpStatus.BAD_GATEWAY, "La clave de Groq no es válida.");
                case 404 -> new ApiException(HttpStatus.BAD_GATEWAY, "No se encontró el modelo de Groq configurado (GROQ_MODEL).");
                default -> new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo analizar la foto (error " + codigo + " de Groq).");
            };
        } catch (ResourceAccessException e) {
            log.warn("Groq ({}) no respondió a tiempo", modelo);
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "La IA tardó demasiado en responder. Intenta de nuevo.");
        }
    }

    private static String textoDe(Map<?, ?> respuesta) {
        try {
            Map<?, ?> eleccion = (Map<?, ?>) ((List<?>) respuesta.get("choices")).get(0);
            Object contenido = ((Map<?, ?>) eleccion.get("message")).get("content");
            return contenido == null ? "" : contenido.toString();
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "La IA no devolvió un resultado válido.");
        }
    }
}
