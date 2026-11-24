package com.acuimuestra.backend.service;

import com.acuimuestra.backend.dto.AnalisisIaResponse;
import com.acuimuestra.backend.model.FotoMuestra;
import com.acuimuestra.backend.model.Muestra;
import com.acuimuestra.backend.model.Rol;
import com.acuimuestra.backend.repository.FotoMuestraRepository;
import com.acuimuestra.backend.repository.MuestraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AnalisisIaService {

    public static final double UMBRAL_DIFERENCIA = 20.0;

    private final MuestraRepository muestras;
    private final FotoMuestraRepository fotos;
    private final AccesoService acceso;
    private final AnalizadorIa analizador;

    public AnalisisIaService(MuestraRepository muestras, FotoMuestraRepository fotos,
                             AccesoService acceso, AnalizadorIa analizador) {
        this.muestras = muestras;
        this.fotos = fotos;
        this.acceso = acceso;
        this.analizador = analizador;
    }

    public AnalisisIaResponse analizar(String usuario, String id) {
        acceso.requerir(usuario, Rol.SUPERVISOR, Rol.ANALISTA);
        Muestra muestra = muestras.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe la muestra " + id));
        byte[] foto = fotos.findById(id)
                .map(FotoMuestra::getDatos)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La muestra " + id + " no tiene foto en el servidor"));
        if (!analizador.configurado()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El análisis con IA no está configurado en el servidor (GEMINI_API_KEY)");
        }
        ResultadoIa resultado = analizador.analizar(foto);
        return armar(resultado, muestra.getConteo(), resultado.modelo() != null ? resultado.modelo() : analizador.modelo());
    }

    static AnalisisIaResponse armar(ResultadoIa ia, int conteoOperador, String modelo) {
        Double diferencia = conteoOperador > 0
                ? Math.round(Math.abs(ia.conteoEstimado() - conteoOperador) * 1000.0 / conteoOperador) / 10.0
                : null;
        boolean revisar = (diferencia != null && diferencia > UMBRAL_DIFERENCIA)
                || ia.hayCholgas()
                || "mala".equals(ia.calidadFoto());
        return new AnalisisIaResponse(ia.conteoEstimado(), ia.hayCholgas(), ia.calidadFoto(), ia.comentario(),
                conteoOperador, diferencia, revisar, modelo);
    }
}
