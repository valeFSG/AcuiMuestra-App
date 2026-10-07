package com.acuimuestra.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.Muestra
import kotlinx.coroutines.flow.Flow

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
}