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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class GeminiAnalizador implements AnalizadorIa {

    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/{modelo}:generateContent";
    private static final int SERVICIO_OCUPADO = 503;
    private static final int LIMITE_ALCANZADO = 429;
    private static final int MODELO_NO_EXISTE = 404;
    private static final long TIEMPO_MAXIMO_MS = 10_000;
    private static final Logger log = LoggerFactory.getLogger(GeminiAnalizador.class);

    static final String INSTRUCCION = RespuestaIa.INSTRUCCION;


    private final RestClient http;
    private final String clave;
    private final List<String> modelos;

    public GeminiAnalizador(@Value("${acuimuestra.gemini.api-key:}") String clave,
                            @Value("${acuimuestra.gemini.modelo:gemini-flash-latest}") String modelo,
                            @Value("${acuimuestra.gemini.respaldo:gemini-flash-lite-latest}") String respaldo) {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory();
        fabrica.setReadTimeout(Duration.ofSeconds(10));
        this.http = RestClient.builder().requestFactory(fabrica).build();
        this.clave = clave == null ? "" : clave.strip();
        List<String> lista = new ArrayList<>();
        for (String nombre : ((modelo == null ? "" : modelo) + "," + (respaldo == null ? "" : respaldo)).split(",")) {
            String limpio = nombre.strip();
            if (!limpio.isEmpty() && !lista.contains(limpio)) lista.add(limpio);
        }
        if (lista.isEmpty()) lista.add("gemini-flash-latest");
        this.modelos = List.copyOf(lista);
    }

    @Override
    public boolean configurado() {
        return !clave.isEmpty();
    }

    @Override
    public String modelo() {
        return modelos.get(0);
    }

    @Override
    public ResultadoIa analizar(byte[] fotoJpeg) {
        Map<String, Object> cuerpo = Map.of(
                "contents", List.of(Map.of("parts", List.of(
                        Map.of("inline_data", Map.of(
                                "mime_type", "image/jpeg",
                                "data", Base64.getEncoder().encodeToString(fotoJpeg))),
                        Map.of("text", INSTRUCCION)))),
                "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0));

        RestClientResponseException error = null;
        boolean huboDemora = false;
        long inicio = System.currentTimeMillis();
        for (String actual : modelos) {
            long restante = TIEMPO_MAXIMO_MS - (System.currentTimeMillis() - inicio);
            if (restante < 1_500) {
                huboDemora = true;
                break;
            }
            try {
                Map<?, ?> respuesta = CompletableFuture
                        .supplyAsync(() -> llamar(actual, cuerpo))
                        .get(restante, TimeUnit.MILLISECONDS);
                return interpretar(textoDe(respuesta)).conModelo(actual);
            } catch (TimeoutException e) {
                huboDemora = true;
                log.warn("Gemini ({}) superó el máximo de {} s", actual, TIEMPO_MAXIMO_MS / 1000);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Se interrumpió el análisis con IA.");
            } catch (ExecutionException e) {
                Throwable causa = e.getCause();
                if (causa instanceof RestClientResponseException respuestaError) {
                    error = respuestaError;
                    int codigo = respuestaError.getStatusCode().value();
                    log.warn("Gemini ({}) respondió {}: {}", actual, codigo, resumen(respuestaError.getResponseBodyAsString()));
                    if (codigo != SERVICIO_OCUPADO && codigo != LIMITE_ALCANZADO && codigo != MODELO_NO_EXISTE) break;
                } else if (causa instanceof ResourceAccessException) {
                    huboDemora = true;
                    log.warn("Gemini ({}) no respondió a tiempo", actual);
                } else {
                    throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo analizar la foto con Gemini.");
                }
            }
        }
        if (error == null && huboDemora) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "La IA tardó demasiado en responder. Intenta de nuevo.");
        }
        throw traducir(error);
    }

    private Map<?, ?> llamar(String modeloActual, Map<String, Object> cuerpo) {
        return http.post()
                .uri(URL, modeloActual)
                .header("x-goog-api-key", clave)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(Map.class);
    }

    private static String resumen(String cuerpo) {
        String limpio = cuerpo == null ? "" : cuerpo.replaceAll("\\s+", " ");
        return limpio.length() > 300 ? limpio.substring(0, 300) + "…" : limpio;
    }

    static String textoDe(Map<?, ?> respuesta) {
        try {
            Map<?, ?> candidato = (Map<?, ?>) ((List<?>) respuesta.get("candidates")).get(0);
            List<?> partes = (List<?>) ((Map<?, ?>) candidato.get("content")).get("parts");
            StringBuilder texto = new StringBuilder();
            for (Object parte : partes) {
                Object t = ((Map<?, ?>) parte).get("text");
                if (t != null) texto.append(t);
            }
            return texto.toString();
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "La IA no devolvió un resultado válido.");
        }
    }

    static ResultadoIa interpretar(String texto) {
        return RespuestaIa.interpretar(texto);
    }

    private static ApiException traducir(RestClientResponseException e) {
        int codigo = e == null ? 0 : e.getStatusCode().value();
        return switch (codigo) {
            case 429 -> new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Se alcanzó el límite gratuito de la IA. Intenta en unos minutos.");
            case 503 -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "La IA está ocupada. Intenta en un momento.");
            case 400, 401, 403 -> new ApiException(HttpStatus.BAD_GATEWAY, "La clave de la IA no es válida o no tiene permiso.");
            case 404 -> new ApiException(HttpStatus.BAD_GATEWAY, "No se encontró el modelo de IA configurado (GEMINI_MODEL).");
            default -> new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo analizar la foto (error " + codigo + " de la IA).");
        };
    }

}
