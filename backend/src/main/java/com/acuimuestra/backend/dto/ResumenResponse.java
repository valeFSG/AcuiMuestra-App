package com.acuimuestra.backend.dto;

import java.util.Map;

public record ResumenResponse(
        long totalMuestras,
        long individuosContados,
        Map<String, Long> porEstado,
        Map<String, Long> porCentro,
        Map<String, Double> densidadPromedioPorCentro
) {}
