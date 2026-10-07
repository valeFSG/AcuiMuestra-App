package com.acuimuestra.app.data.repository

import com.acuimuestra.app.data.local.MuestraDao
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.Muestra
import kotlinx.coroutines.flow.Flow

data class FiltroMuestras(
    val centroId: Long? = null,
    val lineaId: Long? = null,
    val operadorId: Long? = null,
    val estado: EstadoRevision? = null,
    val desde: Long? = null,
    val hasta: Long? = null
)

class MuestraRepository(private val dao: MuestraDao) {

    suspend fun guardar(muestra: Muestra): Long =
        if (muestra.id == 0L) dao.insertar(muestra)
        else dao.actualizar(muestra).let { muestra.id }

    fun observar(id: Long): Flow<Muestra?> = dao.observarPorId(id)

    fun filtrar(filtro: FiltroMuestras = FiltroMuestras()): Flow<List<Muestra>> =
        dao.filtrar(filtro.centroId, filtro.lineaId, filtro.operadorId, filtro.estado, filtro.desde, filtro.hasta)

    suspend fun revisar(id: Long, estado: EstadoRevision, comentario: String?) =
        dao.actualizarEstado(id, estado, comentario?.trim()?.ifBlank { null })
}