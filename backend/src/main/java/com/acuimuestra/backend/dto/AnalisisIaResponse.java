package com.acuimuestra.backend.dto;

public record AnalisisIaResponse(
        int conteoEstimado,
        boolean hayCholgas,
        String calidadFoto,
        String comentario,
        int conteoOperador,
        Double diferenciaPorcentaje,
        boolean requiereRevision,
        String modelo) {}
