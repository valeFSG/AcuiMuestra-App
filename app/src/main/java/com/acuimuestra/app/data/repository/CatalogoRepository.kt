package com.acuimuestra.app.data.repository

import com.acuimuestra.app.data.local.CatalogoDao
import com.acuimuestra.app.data.model.Centro
import com.acuimuestra.app.data.model.Linea
import com.acuimuestra.app.data.model.Tren
import kotlinx.coroutines.flow.Flow

class CatalogoRepository(private val dao: CatalogoDao) {
    fun centros(): Flow<List<Centro>> = dao.centros()
    fun trenes(centroId: Long): Flow<List<Tren>> = dao.trenesDeCentro(centroId)
    fun lineas(trenId: Long): Flow<List<Linea>> = dao.lineasDeTren(trenId)
}