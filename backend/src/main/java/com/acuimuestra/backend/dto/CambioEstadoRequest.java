package com.acuimuestra.backend.dto;

import com.acuimuestra.backend.model.EstadoRevision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambioEstadoRequest(
        @NotNull(message = "El estado es obligatorio") EstadoRevision estado,
        @Size(max = 500, message = "Máximo 500 caracteres") String comentario
) {}
