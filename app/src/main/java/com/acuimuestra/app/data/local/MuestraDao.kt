package com.acuimuestra.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.Muestra
import kotlinx.coroutines.flow.Flow
import com.acuimuestra.app.data.model.MuestraResumen

@Dao
interface MuestraDao {
    @Insert
    suspend fun insertar(muestra: Muestra): Long

    @Update
    suspend fun actualizar(muestra: Muestra)

    @Query("SELECT * FROM muestras WHERE id = :id")
    fun observarPorId(id: Long): Flow<Muestra?>

    @Query(
        """
        SELECT * FROM muestras
        WHERE (:centroId IS NULL OR centroId = :centroId)
          AND (:lineaId IS NULL OR lineaId = :lineaId)
          AND (:operadorId IS NULL OR operadorId = :operadorId)
          AND (:estado IS NULL OR estado = :estado)
          AND (:desde IS NULL OR fechaHora >= :desde)
          AND (:hasta IS NULL OR fechaHora <= :hasta)
        ORDER BY fechaHora DESC
        """
    )
    fun filtrar(
        centroId: Long? = null,
        lineaId: Long? = null,
        operadorId: Long? = null,
        estado: EstadoRevision? = null,
        desde: Long? = null,
        hasta: Long? = null
    ): Flow<List<Muestra>>

    @Query("UPDATE muestras SET estado = :estado, comentarioSupervisor = :comentario WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: EstadoRevision, comentario: String?)

    @Query(
        """
        SELECT m.id, m.fechaHora, m.conteo, m.estado,
               c.nombre AS centroNombre, t.nombre AS trenNombre,
               l.nombre AS lineaNombre, u.nombreVisible AS operadorNombre
        FROM muestras m
        JOIN centros c ON c.id = m.centroId
        JOIN trenes t ON t.id = m.trenId
        JOIN lineas l ON l.id = m.lineaId
        JOIN usuarios u ON u.id = m.operadorId
        WHERE (:centroId IS NULL OR m.centroId = :centroId)
          AND (:lineaId IS NULL OR m.lineaId = :lineaId)
          AND (:operadorId IS NULL OR m.operadorId = :operadorId)
          AND (:estado IS NULL OR m.estado = :estado)
          AND (:desde IS NULL OR m.fechaHora >= :desde)
          AND (:hasta IS NULL OR m.fechaHora <= :hasta)
        ORDER BY m.fechaHora DESC
        """
    )
    fun resumen(
        centroId: Long? = null,
        lineaId: Long? = null,
        operadorId: Long? = null,
        estado: EstadoRevision? = null,
        desde: Long? = null,
        hasta: Long? = null
    ): Flow<List<MuestraResumen>>
}