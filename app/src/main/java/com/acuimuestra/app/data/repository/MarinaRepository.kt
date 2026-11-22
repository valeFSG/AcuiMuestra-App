package com.acuimuestra.app.data.repository

import com.acuimuestra.app.data.remote.MarinaApi

data class CondicionMarina(
    val alturaOlaM: Double?,
    val temperaturaMarC: Double?,
    val hora: String?
)

class MarinaRepository(
    private val api: MarinaApi,
    private val latitud: Double = LATITUD_REFERENCIA,
    private val longitud: Double = LONGITUD_REFERENCIA
) {

    suspend fun condicionesActuales(): Result<CondicionMarina> = runCatching {
        val actual = checkNotNull(api.condicionesActuales(latitud, longitud).current) { "Sin datos marinos" }
        check(actual.alturaOlaM != null || actual.temperaturaMarC != null) { "Sin datos marinos" }
        CondicionMarina(actual.alturaOlaM, actual.temperaturaMarC, actual.time)
    }

    companion object {
        const val LATITUD_REFERENCIA = -41.85
        const val LONGITUD_REFERENCIA = -73.05
        const val NOMBRE_REFERENCIA = "Golfo de Ancud"
    }
}