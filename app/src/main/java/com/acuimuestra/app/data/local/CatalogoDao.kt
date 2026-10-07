package com.acuimuestra.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.acuimuestra.app.data.model.Centro
import com.acuimuestra.app.data.model.Linea
import com.acuimuestra.app.data.model.Tren
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogoDao {
    @Query("SELECT * FROM centros ORDER BY nombre")
    fun centros(): Flow<List<Centro>>

    @Query("SELECT * FROM trenes WHERE centroId = :centroId ORDER BY nombre")
    fun trenesDeCentro(centroId: Long): Flow<List<Tren>>

    @Query("SELECT * FROM lineas WHERE trenId = :trenId ORDER BY nombre")
    fun lineasDeTren(trenId: Long): Flow<List<Linea>>

    @Query("SELECT COUNT(*) FROM centros")
    suspend fun contarCentros(): Int

    @Insert suspend fun insertarCentro(centro: Centro): Long
    @Insert suspend fun insertarTren(tren: Tren): Long
    @Insert suspend fun insertarLinea(linea: Linea): Long
}