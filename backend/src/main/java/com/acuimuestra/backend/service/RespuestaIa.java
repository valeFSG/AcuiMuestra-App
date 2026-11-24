package com.acuimuestra.backend.service;

import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RespuestaIa {

    static final String INSTRUCCION = """
            Eres un asistente de control de calidad para muestras de chorito (Mytilus chilensis) \
            de un centro de cultivo en Chile. Analiza la foto y responde SOLO con un objeto JSON con estas claves:
            "conteoEstimado": número entero de choritos visibles (0 si no hay),
            "hayCholgas": true si ves cholgas (Aulacomya atra), que se distinguen por su concha con estrías marcadas,
            "calidadFoto": "buena", "regular" o "mala", según si la nitidez y la luz permiten contar,
            "comentario": una frase corta en español sobre la muestra.
            """;

    private static final Pattern CONTEO = Pattern.compile("\"conteoEstimado\"\\s*:\\s*(\\d{1,6})");
    private static final Pattern CHOLGAS = Pattern.compile("\"hayCholgas\"\\s*:\\s*(true|false)");
    private static final Pattern CALIDAD = Pattern.compile("\"calidadFoto\"\\s*:\\s*\"(buena|regular|mala)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMENTARIO = Pattern.compile("\"comentario\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    private RespuestaIa() {}

    static ResultadoIa interpretar(String texto) {
        String json = ultimoJson(texto == null ? "" : texto);
        Matcher conteo = CONTEO.matcher(json);
        if (!conteo.find()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "La IA no devolvió un conteo.");
        }
        Matcher cholgas = CHOLGAS.matcher(json);
        Matcher calidad = CALIDAD.matcher(json);
        Matcher comentario = COMENTARIO.matcher(json);
        return new ResultadoIa(
                Integer.parseInt(conteo.group(1)),
                cholgas.find() && Boolean.parseBoolean(cholgas.group(1)),
                calidad.find() ? calidad.group(1).toLowerCase(Locale.ROOT) : "regular",
                comentario.find() ? comentario.group(1).replace("\\\"", "\"").replace("\\n", " ").strip() : "");
    }

    private static String ultimoJson(String texto) {
        int clave = texto.lastIndexOf("\"conteoEstimado\"");
        if (clave < 0) return texto;
        int inicio = texto.lastIndexOf('{', clave);
        return inicio < 0 ? texto.substring(clave) : texto.substring(inicio);
    }
}
