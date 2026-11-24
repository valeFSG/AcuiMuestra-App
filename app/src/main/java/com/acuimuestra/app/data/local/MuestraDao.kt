package com.acuimuestra.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.Muestra
import com.acuimuestra.app.data.model.MuestraDetalle
import com.acuimuestra.app.data.model.MuestraPendiente
import com.acuimuestra.app.data.model.MuestraResumen
import com.acuimuestra.app.data.model.Ubicacion
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

    @Query(
        """
        UPDATE muestras SET estado = :estado, comentarioSupervisor = :comentario,
            usuarioCambio = :usuario, revisadoPor = :usuario, sincronizada = 0
        WHERE id = :id
        """
    )
    suspend fun actualizarEstado(id: Long, estado: EstadoRevision, comentario: String?, usuario: String?)

    @Query(
        """
        SELECT m.id, m.uuid, m.fechaHora, m.conteo, m.estado, m.sincronizada, m.tramoCm, m.observaciones,
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

    @Query(
        """
        SELECT m.id, m.uuid, m.fechaHora, m.tramoCm, m.conteo, m.observaciones, m.fotoUri,
               m.calibrePromedioMm, m.calibreMinMm, m.calibreMaxMm, m.pesoGramos,
               m.estado, m.comentarioSupervisor, m.horaVerificada, m.sincronizada, m.conteoMarcado,
               c.nombre AS centroNombre, t.nombre AS trenNombre,
               l.nombre AS lineaNombre, u.nombreVisible AS operadorNombre,
               r.nombreVisible AS revisorNombre
        FROM muestras m
        JOIN centros c ON c.id = m.centroId
        JOIN trenes t ON t.id = m.trenId
        JOIN lineas l ON l.id = m.lineaId
        JOIN usuarios u ON u.id = m.operadorId
        LEFT JOIN usuarios r ON r.usuario = m.revisadoPor
        WHERE m.id = :id
        """
    )
    fun observarDetalle(id: Long): Flow<MuestraDetalle?>

    @Query(
        """
        UPDATE muestras SET conteo = :conteo, tramoCm = :tramoCm, observaciones = :observaciones,
            calibrePromedioMm = :calibre, pesoGramos = :peso, fotoUri = :fotoUri, estado = :estado,
            usuarioCambio = :usuario, sincronizada = 0
        WHERE id = :id
        """
    )
    suspend fun corregir(
        id: Long,
        conteo: Int,
        tramoCm: Int,
        observaciones: String,
        calibre: Double?,
        peso: Double?,
        fotoUri: String?,
        estado: EstadoRevision,
        usuario: String?
    )

    @Query(
        """
        SELECT m.*, c.nombre AS centroNombre, t.nombre AS trenNombre,
               l.nombre AS lineaNombre, u.usuario AS operadorUsuario
        FROM muestras m
        JOIN centros c ON c.id = m.centroId
        JOIN trenes t ON t.id = m.trenId
        JOIN lineas l ON l.id = m.lineaId
        JOIN usuarios u ON u.id = m.operadorId
        WHERE m.sincronizada = 0
        ORDER BY m.fechaHora
        """
    )
    suspend fun pendientes(): List<MuestraPendiente>

    @Query("SELECT COUNT(*) FROM muestras WHERE sincronizada = 0")
    fun contarPendientes(): Flow<Int>

    @Query("UPDATE muestras SET sincronizada = 1 WHERE uuid = :uuid")
    suspend fun marcarSincronizada(uuid: String)

    @Query("SELECT * FROM muestras WHERE uuid = :uuid LIMIT 1")
    suspend fun buscarPorUuid(uuid: String): Muestra?

    @Query(
        """
        SELECT c.id AS centroId, t.id AS trenId, l.id AS lineaId
        FROM centros c
        JOIN trenes t ON t.centroId = c.id
        JOIN lineas l ON l.trenId = t.id
        WHERE c.nombre = :centro AND t.nombre = :tren AND l.nombre = :linea
        LIMIT 1
        """
    )
    suspend fun buscarUbicacion(centro: String, tren: String, linea: String): Ubicacion?
}