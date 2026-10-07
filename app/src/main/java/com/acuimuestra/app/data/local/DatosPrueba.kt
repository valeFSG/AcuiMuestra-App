package com.acuimuestra.app.data.local

import androidx.room.withTransaction
import com.acuimuestra.app.data.model.*

object DatosPrueba {

    suspend fun cargarSiVacia(db: AcuiMuestraDatabase) {
        if (db.usuarioDao().contar() > 0) return
        db.withTransaction {
            db.usuarioDao().insertarTodos(
                listOf(
                    Usuario(id = 1, usuario = "operador", clave = "1234", nombreVisible = "Operador Demo", rol = Rol.OPERADOR),
                    Usuario(id = 2, usuario = "supervisor", clave = "1234", nombreVisible = "Supervisor Demo", rol = Rol.SUPERVISOR)
                )
            )

            val catalogo = db.catalogoDao()
            val ubicaciones = mutableListOf<Triple<Long, Long, Long>>()
            listOf("Centro Ficticio Norte", "Centro Ficticio Sur").forEach { nombreCentro ->
                val centroId = catalogo.insertarCentro(Centro(nombre = nombreCentro))
                (1..2).forEach { t ->
                    val trenId = catalogo.insertarTren(Tren(centroId = centroId, nombre = "Tren $t"))
                    (1..3).forEach { l ->
                        val lineaId = catalogo.insertarLinea(Linea(trenId = trenId, nombre = "Línea $l"))
                        ubicaciones += Triple(centroId, trenId, lineaId)
                    }
                }
            }

            val ahora = System.currentTimeMillis()
            val unDia = 86_400_000L
            EstadoRevision.entries.forEachIndexed { i, estado ->
                val (centroId, trenId, lineaId) = ubicaciones[i * 3]
                db.muestraDao().insertar(
                    Muestra(
                        centroId = centroId,
                        trenId = trenId,
                        lineaId = lineaId,
                        operadorId = 1,
                        fechaHora = ahora - i * unDia,
                        tramoCm = 20,
                        conteo = 150 + i * 12,
                        observaciones = "Muestra de prueba ${i + 1}",
                        estado = estado
                    )
                )
            }
        }
    }
}