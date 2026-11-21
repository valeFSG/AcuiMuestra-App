package com.acuimuestra.backend.dto;

import jakarta.validation.constraints.*;

public record MuestraRequest(
        @NotBlank(message = "El id es obligatorio") @Size(max = 36, message = "Máximo 36 caracteres") String id,
        @NotBlank(message = "El centro es obligatorio") String centro,
        @NotBlank(message = "El tren es obligatorio") String tren,
        @NotBlank(message = "La línea es obligatoria") String linea,
        @NotNull(message = "El tramo es obligatorio") @Min(value = 1, message = "Debe ser mayor a 0")
        @Max(value = 500, message = "Debe ser menor o igual a 500") Integer tramoCm,
        @NotNull(message = "El conteo es obligatorio") @Min(value = 1, message = "Debe ser mayor a 0")
        @Max(value = 100000, message = "Debe ser menor o igual a 100000") Integer conteo,
        @Size(max = 200, message = "Máximo 200 caracteres") String observaciones,
        @DecimalMin(value = "0", inclusive = false, message = "Debe ser mayor a 0")
        @DecimalMax(value = "150", message = "Debe ser menor o igual a 150 mm") Double calibrePromedioMm,
        @DecimalMin(value = "0", inclusive = false, message = "Debe ser mayor a 0")
        @DecimalMax(value = "10000", message = "Debe ser menor o igual a 10000 g") Double pesoGramos,
        @NotNull(message = "La fecha y hora es obligatoria") Long fechaHora,
        Boolean horaVerificada,
        @Size(max = 120) String fotoNombre,
        @Min(value = 1, message = "Debe ser mayor a 0") Integer conteoMarcado
) {}
